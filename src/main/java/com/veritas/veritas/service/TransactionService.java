package com.veritas.veritas.service;

import com.veritas.veritas.model.TransactionCheck;
import com.veritas.veritas.pipeline.FraudPipeline;
import com.veritas.veritas.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private FraudPipeline fraudPipeline;

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public TransactionCheck checkTransaction(TransactionCheck transaction) {

        System.out.println("=== VERITAS: Checking transaction from " + transaction.getSender());

        // Step 1 - Run the fraud pipeline
        int riskScore = fraudPipeline.runPipeline(transaction);
        String riskLevel = fraudPipeline.getRiskLevel(riskScore);

        System.out.println("=== Risk Level: " + riskLevel + " (Score: " + riskScore + ")");

        // Step 2 - Ask Groq AI to explain why
        String prompt = "You are a fraud detection AI. A transaction has been flagged with risk level: "
                + riskLevel + " and risk score: " + riskScore + "/100.\n\n"
                + "Transaction details:\n"
                + "- Sender: " + transaction.getSender() + "\n"
                + "- Receiver: " + transaction.getReceiver() + "\n"
                + "- Amount: ₹" + transaction.getAmount() + "\n"
                + "- Description: " + transaction.getDescription() + "\n\n"
                + "In 2-3 sentences, explain why this transaction is " + riskLevel
                + " and what the user should do.";

        System.out.println("=== Calling Groq AI for explanation...");
        String aiReason = callGroq(prompt);
        System.out.println("=== Groq done!");

        // Step 3 - Save to DB
        transaction.setRiskScore(riskScore);
        transaction.setRiskLevel(riskLevel);
        transaction.setAiReason(aiReason);

        return repository.save(transaction);
    }

    private String callGroq(String prompt) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama-3.3-70b-versatile");
        body.put("max_tokens", 300);
        body.put("messages", List.of(message));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    public List<TransactionCheck> getHistory() {
        return repository.findAll();
    }

    public Optional<TransactionCheck> getById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
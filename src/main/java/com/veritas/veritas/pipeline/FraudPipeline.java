package com.veritas.veritas.pipeline;

import com.veritas.veritas.model.TransactionCheck;
import com.veritas.veritas.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FraudPipeline {

    @Autowired
    private TransactionRepository repository;

    public int runPipeline(TransactionCheck transaction) {
        int riskScore = 0;

        // ── Stage 1: Amount Check ──
        riskScore += stageAmountCheck(transaction.getAmount());

        // ── Stage 2: Sender == Receiver Check ──
        riskScore += stageSelfTransfer(transaction.getSender(), transaction.getReceiver());

        // ── Stage 3: Frequency Check ──
        riskScore += stageFrequencyCheck(transaction.getSender());

        // ── Stage 4: Time Check ──
        riskScore += stageTimeCheck();

        // ── Stage 5: Round Number Check ──
        riskScore += stageRoundNumber(transaction.getAmount());

        System.out.println("=== PIPELINE COMPLETE | Total Risk Score: " + riskScore);
        return riskScore;
    }

    // Stage 1 — High amount = high risk
    private int stageAmountCheck(double amount) {
        if (amount > 100000) {
            System.out.println("Stage 1: Amount > 1,00,000 → +40");
            return 40;
        } else if (amount > 50000) {
            System.out.println("Stage 1: Amount > 50,000 → +20");
            return 20;
        }
        System.out.println("Stage 1: Amount normal → +0");
        return 0;
    }

    // Stage 2 — Sending money to yourself
    private int stageSelfTransfer(String sender, String receiver) {
        if (sender != null && sender.equalsIgnoreCase(receiver)) {
            System.out.println("Stage 2: Sender == Receiver → +50");
            return 50;
        }
        System.out.println("Stage 2: Different sender/receiver → +0");
        return 0;
    }

    // Stage 3 — Same sender did 5+ transactions in last 1 hour
    private int stageFrequencyCheck(String sender) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<TransactionCheck> recent = repository.findBySender(sender);
        long recentCount = recent.stream()
                .filter(t -> t.getCheckedAt() != null && t.getCheckedAt().isAfter(oneHourAgo))
                .count();

        if (recentCount >= 5) {
            System.out.println("Stage 3: " + recentCount + " transactions in 1 hour → +30");
            return 30;
        }
        System.out.println("Stage 3: Frequency normal → +0");
        return 0;
    }

    // Stage 4 — Late night transactions (12am - 5am) are riskier
    private int stageTimeCheck() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 0 && hour <= 5) {
            System.out.println("Stage 4: Late night transaction → +15");
            return 15;
        }
        System.out.println("Stage 4: Normal hours → +0");
        return 0;
    }

    // Stage 5 — Suspiciously round numbers
    private int stageRoundNumber(double amount) {
        if (amount % 10000 == 0 && amount >= 10000) {
            System.out.println("Stage 5: Round number amount → +10");
            return 10;
        }
        System.out.println("Stage 5: Not a round number → +0");
        return 0;
    }

    // Convert score to risk level
    public String getRiskLevel(int score) {
        if (score >= 61) return "FRAUDULENT";
        else if (score >= 31) return "SUSPICIOUS";
        else return "SAFE";
    }
}
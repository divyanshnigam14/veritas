package com.veritas.veritas.controller;

import com.veritas.veritas.model.TransactionCheck;
import com.veritas.veritas.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // POST /transaction/check
    @PostMapping("/check")
    public ResponseEntity<TransactionCheck> check(@RequestBody TransactionCheck transaction) {
        return ResponseEntity.ok(transactionService.checkTransaction(transaction));
    }

    // GET /transaction/history
    @GetMapping("/history")
    public ResponseEntity<List<TransactionCheck>> history() {
        return ResponseEntity.ok(transactionService.getHistory());
    }

    // GET /transaction/history/1
    @GetMapping("/history/{id}")
    public ResponseEntity<TransactionCheck> getOne(@PathVariable Long id) {
        return transactionService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /transaction/history/1
    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
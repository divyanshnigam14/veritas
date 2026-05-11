package com.veritas.veritas.repository;

import com.veritas.veritas.model.TransactionCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionCheck, Long> {
    List<TransactionCheck> findBySender(String sender);
}
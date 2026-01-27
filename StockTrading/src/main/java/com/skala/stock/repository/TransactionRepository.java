package com.skala.stock.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skala.stock.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByUserIdAndStockIdOrderByTransactionDateDesc(Long userId, Long stockId);

    // delete 방어용
    boolean existsByUserId(Long userId);
    boolean existsByStockId(Long stockId);
}

package com.skala.stock.controller;

import com.skala.stock.dto.TransactionDto;
import com.skala.stock.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "거래 관리", description = "주식 매수/매도 거래 API")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 거래 내역 조회", description = "특정 사용자의 전체 거래 내역을 조회합니다")
    public ResponseEntity<List<TransactionDto>> getUserTransactions(@PathVariable Long userId) {
        List<TransactionDto> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    // 추가된 메서드들

    @GetMapping("/{id}")
    @Operation(summary = "거래 조회 (ID)", description = "ID로 거래 내역을 조회합니다")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        TransactionDto transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/trade")
    @Operation(summary = "주식 매수/매도", description = "사용자가 주식을 매수하거나 매도합니다")
    public ResponseEntity<TransactionDto> tradeStock(@RequestParam Long stockId,
                                                     @RequestParam Long userId,
                                                     @RequestParam String type,
                                                     @RequestParam Integer quantity) {
        TransactionDto transaction = transactionService.tradeStock(stockId, userId, type, quantity);
        return ResponseEntity.ok(transaction);  
    }  

    @GetMapping("/user/{userId}/stock/{stockId}")
    @Operation(summary = "사용자 특정 주식 거래 내역 조회", description = "특정 사용자의 특정 주식에 대한 거래 내역을 조회합니다")
    public ResponseEntity<List<TransactionDto>> getUserStockTransactions(@PathVariable Long userId, @PathVariable Long stockId) {
    return ResponseEntity.ok(transactionService.getUserStockTransactions(userId, stockId));
}

    
}

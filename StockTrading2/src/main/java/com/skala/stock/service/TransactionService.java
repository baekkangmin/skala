package com.skala.stock.service;

import com.skala.stock.dto.TransactionDto;
import com.skala.stock.entity.Portfolio;
import com.skala.stock.entity.Stock;
import com.skala.stock.entity.Transaction;
import com.skala.stock.entity.Transaction.TransactionType;
import com.skala.stock.entity.User;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.TransactionRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<TransactionDto> getUserTransactions(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 추가된 메서드

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("거래 내역을 찾을 수 없습니다: " + id));
        return convertToDto(transaction);
    }

    @Transactional
    public TransactionDto tradeStock(Long stockId, Long userId, String type, Integer quantity) {
        if (stockId == null || userId == null) {
            throw new IllegalArgumentException("주식 ID와 사용자 ID는 필수입니다.");
        }
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + stockId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        
        long totalAmount = stock.getCurrentPrice() * quantity;
        
        // 매수인 경우 잔액 확인 및 차감
        if ("BUY".equalsIgnoreCase(type)) {
            if (user.getBalance() < totalAmount) {
                throw new RuntimeException("잔액이 부족합니다. 보유: " + user.getBalance() + ", 필요: " + totalAmount);
            }
            user.setBalance(user.getBalance() - totalAmount);
        }
        
        // 매도인 경우 보유 수량 확인 및 잔액 증가
        if ("SELL".equalsIgnoreCase(type)) {
            List<Transaction> userTransactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
            long totalQuantity = userTransactions.stream()
                    .filter(t -> t.getStock().getId().equals(stockId))
                    .mapToLong(t -> TransactionType.BUY.equals(t.getType()) ? t.getQuantity() : -t.getQuantity())
                    .sum();
            
            if (totalQuantity < quantity) {
                throw new RuntimeException("보유 수량이 부족합니다. 보유: " + totalQuantity + ", 요청: " + quantity);
            }
            user.setBalance(user.getBalance() + totalAmount);
        }
        
        // 거래 생성 로직
        Transaction newTransaction = Transaction.builder()
                .stock(stock)
                .user(user)
                .type(TransactionType.valueOf(type.toUpperCase()))
                .quantity(Long.valueOf(quantity))
                .price(stock.getCurrentPrice())
                .totalAmount(totalAmount)
                .transactionDate(null)
                .build();

        userRepository.save(user);
        transactionRepository.save(newTransaction);
        
        // 포트폴리오 업데이트
        updatePortfolio(user, stock, TransactionType.valueOf(type.toUpperCase()), Long.valueOf(quantity), stock.getCurrentPrice());
        
        return convertToDto(newTransaction);
    }
    
    private void updatePortfolio(User user, Stock stock, TransactionType type, Long quantity, Long price) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findByUserIdAndStockId(user.getId(), stock.getId());
        
        if (type == TransactionType.BUY) {
            if (portfolioOpt.isPresent()) {
                // 기존 포트폴리오 업데이트: 평균단가 재계산
                Portfolio portfolio = portfolioOpt.get();
                long totalQuantity = portfolio.getQuantity() + quantity;
                long totalCost = (portfolio.getQuantity() * portfolio.getAveragePrice()) + (quantity * price);
                long newAveragePrice = totalCost / totalQuantity;
                
                portfolio.setQuantity(totalQuantity);
                portfolio.setAveragePrice(newAveragePrice);
                portfolioRepository.save(portfolio);
            } else {
                // 새 포트폴리오 생성
                Portfolio newPortfolio = Portfolio.builder()
                        .user(user)
                        .stock(stock)
                        .quantity(quantity)
                        .averagePrice(price)
                        .build();
                portfolioRepository.save(newPortfolio);
            }
        } else if (type == TransactionType.SELL) {
            if (portfolioOpt.isPresent()) {
                Portfolio portfolio = portfolioOpt.get();
                long remainingQuantity = portfolio.getQuantity() - quantity;
                
                if (remainingQuantity > 0) {
                    portfolio.setQuantity(remainingQuantity);
                    portfolioRepository.save(portfolio);
                } else {
                    // 모두 매도한 경우 포트폴리오에서 제거
                    portfolioRepository.delete(portfolio);
                }
            }
        }
    }

    public List<TransactionDto> getUserStockTransactions(Long userId, Long stockId) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndStockIdOrderByTransactionDateDesc(userId, stockId);
        return transactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }   

    private TransactionDto convertToDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .username(transaction.getUser().getUsername())
                .stockId(transaction.getStock().getId())
                .stockCode(transaction.getStock().getCode())
                .stockName(transaction.getStock().getName())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

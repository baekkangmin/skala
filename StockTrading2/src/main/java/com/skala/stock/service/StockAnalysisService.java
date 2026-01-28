package com.skala.stock.service;

import com.skala.stock.dto.AssetSummaryDto;
import com.skala.stock.dto.DailyTradeSummaryDto;
import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.dto.PortfolioEvaluationDto;
import com.skala.stock.dto.ReturnRateDto;
import com.skala.stock.dto.TransactionDetailResponseDto;
import com.skala.stock.dto.TransactionDto;
import com.skala.stock.entity.Portfolio;
import com.skala.stock.entity.Transaction;
import com.skala.stock.entity.User;
import com.skala.stock.mapper.TransactionStatisticsDto;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.TransactionRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockAnalysisService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    // 이미 CRUD 쪽에서 만들어둔 서비스 재사용
    private final PortfolioService portfolioService;
    private final TransactionService transactionService;

    // 1) 포트폴리오 평가 손익
    public PortfolioEvaluationDto getPortfolioEvaluation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        List<PortfolioDto> holdings = portfolioService.getUserPortfolio(userId);

        long totalCost = 0L;
        long totalMarketValue = 0L;

        for (PortfolioDto p : holdings) {
            totalCost += p.getQuantity() * p.getAveragePrice();
            totalMarketValue += p.getQuantity() * p.getCurrentPrice();
        }

        long profitLoss = totalMarketValue - totalCost;
        double returnRate = (totalCost == 0L) ? 0.0 : (profitLoss * 100.0) / totalCost;
        long totalAssets = user.getBalance() + totalMarketValue;

        return PortfolioEvaluationDto.builder()
                .userId(userId)
                .cashBalance(user.getBalance())
                .totalCost(totalCost)
                .totalMarketValue(totalMarketValue)
                .totalProfitLoss(profitLoss)
                .returnRate(returnRate)
                .totalAssets(totalAssets)
                .holdings(holdings)
                .build();
    }

    // 2) 거래 내역 상세 조회(요약 포함)
    public TransactionDetailResponseDto getTransactionDetails(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        List<TransactionDto> txs = transactionService.getUserTransactions(userId);

        long buy = 0L;
        long sell = 0L;

        for (TransactionDto tx : txs) {
            if (tx.getType() == Transaction.TransactionType.BUY) {
                buy += (tx.getTotalAmount() == null ? 0L : tx.getTotalAmount());
            } else if (tx.getType() == Transaction.TransactionType.SELL) {
                sell += (tx.getTotalAmount() == null ? 0L : tx.getTotalAmount());
            }
        }

        return TransactionDetailResponseDto.builder()
                .userId(userId)
                .totalBuyAmount(buy)
                .totalSellAmount(sell)
                .netAmount(sell - buy)
                .transactions(txs)
                .build();
    }

    // 3) 특정 주식 거래 내역 조회
    public List<TransactionDto> getStockTransactions(Long userId, Long stockId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        return transactionService.getUserStockTransactions(userId, stockId);
    }

    // 4) 총 자산 조회(현금 + 보유주식 평가금액)
    public AssetSummaryDto getTotalAssets(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);

        long stockValue = 0L;
        for (Portfolio p : portfolios) {
            stockValue += p.getQuantity() * p.getStock().getCurrentPrice();
        }

        return AssetSummaryDto.builder()
                .userId(userId)
                .cashBalance(user.getBalance())
                .stockValue(stockValue)
                .totalAssets(user.getBalance() + stockValue)
                .build();
    }

    // 5) 총 수익률 조회(보유 주식 원가 대비)
    public ReturnRateDto getTotalReturnRate(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);

        long totalCost = 0L;
        long totalMarketValue = 0L;

        for (Portfolio p : portfolios) {
            totalCost += p.getQuantity() * p.getAveragePrice();
            totalMarketValue += p.getQuantity() * p.getStock().getCurrentPrice();
        }

        long profitLoss = totalMarketValue - totalCost;
        double returnRate = (totalCost == 0L) ? 0.0 : (profitLoss * 100.0) / totalCost;

        return ReturnRateDto.builder()
                .userId(userId)
                .totalCost(totalCost)
                .totalMarketValue(totalMarketValue)
                .profitLoss(profitLoss)
                .returnRate(returnRate)
                .build();
    }

    // 6) 거래 통계 조회(종목별 집계)
    public List<TransactionStatisticsDto> getTradeStatistics(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        return transactionRepository.getUserTransactionStatistics(userId).stream()
                .map(v -> {
                    long buyQ = v.getTotalBuyQuantity() == null ? 0L : v.getTotalBuyQuantity();
                    long sellQ = v.getTotalSellQuantity() == null ? 0L : v.getTotalSellQuantity();
                    long buyA = v.getTotalBuyAmount() == null ? 0L : v.getTotalBuyAmount();
                    long sellA = v.getTotalSellAmount() == null ? 0L : v.getTotalSellAmount();

                    return TransactionStatisticsDto.builder()
                            .stockCode(v.getStockCode())
                            .stockName(v.getStockName())
                            .totalBuyQuantity(buyQ)
                            .totalSellQuantity(sellQ)
                            .netQuantity(buyQ - sellQ)
                            .totalBuyAmount(buyA)
                            .totalSellAmount(sellA)
                            .netAmount(sellA - buyA)
                            .build();
                })
                .toList();
    }

    // 7) 일별 거래 내역 조회(일자별 집계)
    public List<DailyTradeSummaryDto> getDailyTrades(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        return transactionRepository.getDailyTradeSummary(userId).stream()
                .map(v -> DailyTradeSummaryDto.builder()
                        .tradeDate(v.getTradeDate().toLocalDate())
                        .buyCount(v.getBuyCount())
                        .sellCount(v.getSellCount())
                        .totalCount(v.getTotalCount())
                        .totalAmount(v.getTotalAmount())
                        .build())
                .toList();
    }
}

package com.skala.stock.repository;

import com.skala.stock.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);
    List<Transaction> findByUserIdAndStockIdOrderByTransactionDateDesc(Long userId, Long stockId);

    boolean existsByUserId(Long userId);
    boolean existsByStockId(Long stockId);

    // ==============================
    // 6) 종목별 거래 통계(집계)
    // ==============================
    interface TransactionStatisticsView {
        String getStockCode();
        String getStockName();
        Long getTotalBuyQuantity();
        Long getTotalSellQuantity();
        Long getTotalBuyAmount();
        Long getTotalSellAmount();
    }

    @Query(value = """
            SELECT s.code AS stockCode,
                   s.name AS stockName,
                   COALESCE(SUM(CASE WHEN t.type = 'BUY' THEN t.quantity ELSE 0 END), 0) AS totalBuyQuantity,
                   COALESCE(SUM(CASE WHEN t.type = 'SELL' THEN t.quantity ELSE 0 END), 0) AS totalSellQuantity,
                   COALESCE(SUM(CASE WHEN t.type = 'BUY' THEN t.total_amount ELSE 0 END), 0) AS totalBuyAmount,
                   COALESCE(SUM(CASE WHEN t.type = 'SELL' THEN t.total_amount ELSE 0 END), 0) AS totalSellAmount
            FROM transactions t
            JOIN stocks s ON s.id = t.stock_id
            WHERE t.user_id = :userId
            GROUP BY s.code, s.name
            ORDER BY s.code
            """, nativeQuery = true)
    List<TransactionStatisticsView> getUserTransactionStatistics(@Param("userId") Long userId);

    // ==============================
    // 7) 일별 거래 내역(집계)
    // ==============================
    interface DailyTradeSummaryView {
        java.sql.Date getTradeDate();
        Long getBuyCount();
        Long getSellCount();
        Long getTotalCount();
        Long getTotalAmount();
    }

    @Query(value = """
            SELECT CAST(t.transaction_date AS DATE) AS tradeDate,
                   COALESCE(SUM(CASE WHEN t.type = 'BUY' THEN 1 ELSE 0 END), 0) AS buyCount,
                   COALESCE(SUM(CASE WHEN t.type = 'SELL' THEN 1 ELSE 0 END), 0) AS sellCount,
                   COUNT(*) AS totalCount,
                   COALESCE(SUM(t.total_amount), 0) AS totalAmount
            FROM transactions t
            WHERE t.user_id = :userId
            GROUP BY CAST(t.transaction_date AS DATE)
            ORDER BY tradeDate DESC
            """, nativeQuery = true)
    List<DailyTradeSummaryView> getDailyTradeSummary(@Param("userId") Long userId);
}

package com.skala.stock.controller;

import com.skala.stock.dto.AssetSummaryDto;
import com.skala.stock.dto.DailyTradeSummaryDto;
import com.skala.stock.dto.PortfolioEvaluationDto;
import com.skala.stock.dto.ReturnRateDto;
import com.skala.stock.dto.TransactionDetailResponseDto;
import com.skala.stock.dto.TransactionDto;
import com.skala.stock.mapper.TransactionStatisticsDto;
import com.skala.stock.service.StockAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "분석", description = "포트폴리오/거래 분석 API")
public class StockAnalysisController {

    private final StockAnalysisService stockAnalysisService;

    @GetMapping("/portfolio/{userId}")
    @Operation(summary = "포트폴리오 평가 손익 조회", description = "보유 주식의 평가금액/원가/손익/수익률을 조회합니다")
    public ResponseEntity<PortfolioEvaluationDto> getPortfolioEvaluation(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getPortfolioEvaluation(userId));
    }

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "거래 내역 상세 조회", description = "사용자 거래 내역 + 매수/매도 총액 요약을 조회합니다")
    public ResponseEntity<TransactionDetailResponseDto> getTransactionDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getTransactionDetails(userId));
    }

    @GetMapping("/transactions/{userId}/stock/{stockId}")
    @Operation(summary = "특정 주식 거래 내역 조회", description = "특정 사용자의 특정 주식 거래 내역을 조회합니다")
    public ResponseEntity<List<TransactionDto>> getStockTransactions(@PathVariable Long userId, @PathVariable Long stockId) {
        return ResponseEntity.ok(stockAnalysisService.getStockTransactions(userId, stockId));
    }

    @GetMapping("/assets/{userId}")
    @Operation(summary = "총 자산 조회", description = "현금 + 보유주식 평가금액으로 총 자산을 조회합니다")
    public ResponseEntity<AssetSummaryDto> getTotalAssets(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getTotalAssets(userId));
    }

    @GetMapping("/return-rate/{userId}")
    @Operation(summary = "총 수익률 조회", description = "보유 주식 원가 대비 평가 손익률을 조회합니다")
    public ResponseEntity<ReturnRateDto> getTotalReturnRate(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getTotalReturnRate(userId));
    }

    @GetMapping("/statistics/{userId}")
    @Operation(summary = "거래 통계 조회", description = "종목별 매수/매도 수량 및 금액 통계를 조회합니다")
    public ResponseEntity<List<TransactionStatisticsDto>> getTradeStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getTradeStatistics(userId));
    }

    @GetMapping("/daily/{userId}")
    @Operation(summary = "일별 거래 내역 조회", description = "사용자의 거래를 일자별로 집계해 조회합니다")
    public ResponseEntity<List<DailyTradeSummaryDto>> getDailyTrades(@PathVariable Long userId) {
        return ResponseEntity.ok(stockAnalysisService.getDailyTrades(userId));
    }
}

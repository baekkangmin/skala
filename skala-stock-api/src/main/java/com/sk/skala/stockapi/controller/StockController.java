package com.sk.skala.stockapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.service.StockService;
import com.sk.skala.stockapi.data.common.Response;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "주식 관리 API")
public class StockController {

    private final StockService stockService;

    // 전체 주식 목록 조회 API
    @Operation(summary = "주식 목록 조회", description = "전체 주식 목록을 페이징하여 조회합니다")
    @GetMapping("/list")
    public Response getAllStocks(
            @Parameter(description = "페이지 오프셋", example = "0") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int count) {
        return stockService.getAllStocks(offset, count);
    }

    // 개별 주식 상세 조회 API
    @Operation(summary = "주식 상세 조회", description = "ID로 특정 주식의 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public Response getStockById(@Parameter(description = "주식 ID", example = "1") @PathVariable Long id) {
        return stockService.getStockById(id);
    }

    // 주식 등록 API
    @Operation(summary = "주식 등록", description = "새로운 주식을 등록합니다")
    @PostMapping
    public Response createStock(@Valid @RequestBody Stock stock) {
        return stockService.createStock(stock);
    }

    // 주식 정보 수정 API
    @Operation(summary = "주식 정보 수정", description = "기존 주식의 정보를 수정합니다")
    @PutMapping
    public Response updateStock(@Valid @RequestBody Stock stock) {
        return stockService.updateStock(stock);
    }

    // 주식 삭제 API
    @Operation(summary = "주식 삭제", description = "특정 주식을 삭제합니다")
    @DeleteMapping("/{id}")
    public Response deleteStock(@Parameter(description = "주식 ID", example = "1") @PathVariable Long id) {
        return stockService.deleteStock(id);
    }
}
package com.skala.stock.controller;

import com.skala.stock.dto.StockDto;
import com.skala.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.StoredProcedureParameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Tag(name = "주식 관리", description = "주식 CRUD API")
public class StockController {

    private final StockService stockService;

    @PostMapping
    @Operation(summary = "주식 생성", description = "새로운 주식을 등록합니다")
    public ResponseEntity<StockDto> createStock(@Valid @RequestBody StockDto stockDto) {
        StockDto createdStock = stockService.createStock(stockDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStock);
    }

    @GetMapping("/{id}")
    @Operation(summary = "주식 조회 (ID)", description = "ID로 주식을 조회합니다")
    public ResponseEntity<StockDto> getStockById(@PathVariable Long id) {
        StockDto stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    @GetMapping
    @Operation(summary = "전체 주식 조회", description = "모든 주식을 조회합니다")
    public ResponseEntity<List<StockDto>> getAllStocks() {
        List<StockDto> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }


    // 추가된 메서드들

    @PutMapping("/{id}")
    @Operation(summary = "주식 수정 (ID)", description = "ID로 주식 정보를 수정합니다")
    public ResponseEntity<StockDto> updateStock(@PathVariable Long id, @Valid @RequestBody StockDto stockDto) {
        StockDto updatedStock = stockService.updateStock(id, stockDto);
        return ResponseEntity.ok(updatedStock);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "주식 삭제 (ID)", description = "ID로 주식을 삭제합니다")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/code/{code}") // url 매핑 중복으로 인해 수정
    @Operation(summary = "주식 조회 (코드)", description = "종목 코드로 주식을 조회합니다")
    public ResponseEntity<StockDto> getStockByCode(@PathVariable String code) {
        StockDto stock = stockService.getStockByCode(code);
        return ResponseEntity.ok(stock);
    }
}

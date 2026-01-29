package com.sk.skala.stockapi.service;

import org.springframework.stereotype.Service;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.StockRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

import com.sk.skala.stockapi.exception.ParameterException;


@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    //// 전체 주식 목록 조회
    public Response getAllStocks(int offset, int count)
    {
        validatePaging(offset, count);

        Pageable pageable = PageRequest.of(offset, count, Sort.by(Sort.Direction.DESC, "id"));
        Page<Stock> stockPage = stockRepository.findAll(pageable);

        Object pagedList = toPagedList(stockPage, offset, count);

        return Response.ok(pagedList);
    }

    // 개별 주식 상세 조회
    public Response getStockById(Long id){
        validateId(id);

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        return Response.ok(stock);
    }

    // 주식 등록
    public Response createStock(Stock stock){
        validateCreate(stock);

        // 이름 중복 체크
        stockRepository.findByStockName(stock.getStockName())
                .ifPresent(s -> {
                    throw new ResponseException(Error.DATA_DUPLICATED);
                });

        // 신규 ID 세팅
        stock.setId(0L);

        Stock saved = stockRepository.save(stock);
        return Response.ok(saved);
    }

    // 주식 정보 수정
    public Response updateStock(Stock stock){

        validateUpdate(stock);

        Long id = stock.getId();
        Stock existing = stockRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));
        
        // 이름 중복 체크
        if (!existing.getStockName().equals(stock.getStockName())) {
            stockRepository.findByStockName(stock.getStockName())
                    .ifPresent(s -> {
                        throw new ResponseException(Error.DATA_DUPLICATED);
                    });
        }

        // 기존 엔티티에 반영(필드명은 실제 엔티티에 맞춰 수정)
        existing.setStockName(stock.getStockName());
        existing.setStockPrice(stock.getStockPrice());

        Stock saved = stockRepository.save(existing);
        return Response.ok(saved);
    }
    
    // 주식 삭제
    public Response deleteStock(Long id){
        if (id == null){
            throw new ParameterException("id");
        }

        Stock existing = stockRepository.findById(id)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        stockRepository.delete(existing);
        return Response.ok(true);

    }

    // Validation methods
    private void validatePaging(int offset, int count) {
        if (offset < 0) {
            throw new ParameterException("offset");
        }
        if (count <= 0) {
            throw new ParameterException("count");
        }
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new ParameterException("id");
        }
    }

    private void validateCreate(Stock stock) {
        if (stock == null) {
            throw new ParameterException("stock");
        }
        if (stock.getStockName() == null || stock.getStockName().trim().isEmpty()) {
            throw new ParameterException("stockName");
        }
        if (stock.getStockPrice() == null || stock.getStockPrice() < 0) {
            throw new ParameterException("stockPrice");
        }
    }

    private void validateUpdate(Stock stock) {
        if (stock == null) {
            throw new ParameterException("stock");
        }
        if (stock.getId() == null) {
            throw new ParameterException("id");
        }
        if (stock.getStockName() == null || stock.getStockName().trim().isEmpty()) {
            throw new ParameterException("stockName");
        }
        if (stock.getStockPrice() == null || stock.getStockPrice() < 0) {
            throw new ParameterException("stockPrice");
        }
    }

    private Object toPagedList(Page<Stock> page, int offset, int count) {
        Map<String, Object> result = new HashMap<>();
        result.put("content", page.getContent());
        result.put("totalElements", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());
        result.put("currentPage", offset);
        result.put("pageSize", count);
        result.put("hasNext", page.hasNext());
        result.put("hasPrevious", page.hasPrevious());
        return result;
    }

}

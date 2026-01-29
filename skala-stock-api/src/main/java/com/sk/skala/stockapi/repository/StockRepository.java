package com.sk.skala.stockapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import com.sk.skala.stockapi.data.table.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    //Stock 엔터티를 관리하기 위해 JpaRepository 상속한 인터페이스 구현
    Optional<Stock> findByStockName(String keyword);

}

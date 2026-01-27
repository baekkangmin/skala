package com.skala.stock.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skala.stock.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByCode(String code);
    boolean existsByCode(String code);

    // update 시 "자기 자신(id)은 제외"하고 code 중복 체크
    boolean existsByCodeAndIdNot(String code, Long id);
}

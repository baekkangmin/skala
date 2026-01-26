package com.example.product.mapper;

import com.example.product.domain.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface OrderMapper {
    void insert(Order o);
    Optional<Order> findById(Long id);
    int updateStatus(Long id, String status);
}

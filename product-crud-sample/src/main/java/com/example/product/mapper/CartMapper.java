package com.example.product.mapper;

import com.example.product.domain.Cart;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface CartMapper {
    Optional<Cart> findByMemberId(Long memberId);
    void insert(Cart c);
}

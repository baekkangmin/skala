package com.example.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long memberId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
}
package com.example.product.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemView {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
}
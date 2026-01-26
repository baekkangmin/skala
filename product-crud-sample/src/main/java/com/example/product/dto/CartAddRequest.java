package com.example.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddRequest {
    @NotNull @Min(1)
    private Long memberId;

    @NotNull @Min(1)
    private Long productId;

    @NotNull @Min(1)
    private Integer quantity;
}
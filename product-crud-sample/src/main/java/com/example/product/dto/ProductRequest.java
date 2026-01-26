package com.example.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "상품명은 필수입니다")
    private String name;

    private String description;

    @NotNull(message = "가격은 필수입니다")
    @Min(value = 1, message = "가격은 1 이상이어야 합니다")
    private BigDecimal price;

    @NotNull(message = "재고는 필수입니다")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    private Integer stock;

    @NotBlank(message = "카테고리는 필수입니다")
    private String category;

    @NotBlank(message = "상태는 필수입니다")
    private String status; // ACTIVE / SOLD_OUT / HIDDEN
}

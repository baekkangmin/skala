package com.skala.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReturnRateDto {
    private Long userId;

    private Long totalCost;
    private Long totalMarketValue;
    private Long profitLoss;
    private Double returnRate;
}

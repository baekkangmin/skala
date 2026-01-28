package com.skala.stock.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortfolioEvaluationDto {
    private Long userId;
    private Long cashBalance;

    private Long totalCost;
    private Long totalMarketValue;
    private Long totalProfitLoss;
    private Double returnRate;

    private Long totalAssets;

    private List<PortfolioDto> holdings;
}

package com.skala.stock.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetSummaryDto {
    private Long userId;
    private Long cashBalance;
    private Long stockValue;
    private Long totalAssets;
}

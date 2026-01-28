package com.skala.stock.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransactionDetailResponseDto {
    private Long userId;

    private Long totalBuyAmount;
    private Long totalSellAmount;
    private Long netAmount;

    private List<TransactionDto> transactions;
}

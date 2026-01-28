package com.skala.stock.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyTradeSummaryDto {
    private LocalDate tradeDate;

    private Long buyCount;
    private Long sellCount;
    private Long totalCount;

    private Long totalAmount;
}

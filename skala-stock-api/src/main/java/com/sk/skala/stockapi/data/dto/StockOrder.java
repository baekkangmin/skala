package com.sk.skala.stockapi.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockOrder {
	private String playerId;
	
	@NotNull(message = "stockId는 필수입니다")
	private Long stockId;
	
	@NotNull(message = "quantity는 필수입니다")
	@Min(value = 1, message = "quantity는 1 이상이어야 합니다")
	private Long quantity;
}

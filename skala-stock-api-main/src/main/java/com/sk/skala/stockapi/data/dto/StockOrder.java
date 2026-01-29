package com.sk.skala.stockapi.data.dto;

import lombok.Data;

@Data
public class StockOrder {
	private String playerId;
	private long stockId;
	private int stockQuantity;
}

package com.sk.skala.stockapi.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // 빌더 패턴을 사용하여 메서드 체이닝 방식으로 세팅
public class PlayerStockDto {
	private Long stockId;
	private String stockName;
	private Double stockPrice;
	private int quantity; // 플레이어의 보유 수량
}
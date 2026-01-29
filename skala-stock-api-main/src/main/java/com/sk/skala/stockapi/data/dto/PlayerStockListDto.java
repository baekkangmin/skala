package com.sk.skala.stockapi.data.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // 빌더 패턴을 사용하여 메서드 체이닝 방식으로 세팅
public class PlayerStockListDto {
	private String playerId;
	private double playerMoney;
	private List<PlayerStockDto> stocks; // 보유 주식 목록
}
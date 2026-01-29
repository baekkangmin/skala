package com.sk.skala.stockapi.data.dto;

import java.util.List;

import lombok.Data;

@Data
public class PlayerStockListDto {
    //Player가 보유한 주식 목록 조회 응답 – Builder 패턴으로 메서드 체이닝 제공
    
    private String playerId;
    private Double playerMoney;
    private List<PlayerStockDto> stocks;

}
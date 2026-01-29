package com.sk.skala.stockapi.data.dto;

import com.sk.skala.stockapi.data.table.PlayerStock;

import lombok.Data;

@Data
public class PlayerStockDto {

    private Long stockId;
    private String stockName;
    private Double stockPrice;
    private Long quantity;

    public static PlayerStockDto from(PlayerStock ps) {
        PlayerStockDto dto = new PlayerStockDto();
        dto.setStockId(ps.getStock().getId());
        dto.setStockName(ps.getStock().getStockName());
        dto.setStockPrice(ps.getStock().getStockPrice());
        dto.setQuantity(ps.getQuantity());
        return dto;
    }

}
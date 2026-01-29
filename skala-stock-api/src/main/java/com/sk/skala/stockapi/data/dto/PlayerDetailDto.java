package com.sk.skala.stockapi.data.dto;

import java.util.List;

import com.sk.skala.stockapi.data.table.Player;

import lombok.Data;

@Data
public class PlayerDetailDto {
    private String playerId;
    private Double playerMoney;
    private List<PlayerStockDto> holdings;

    public static PlayerDetailDto from(Player player, List<PlayerStockDto> holdings) {
        PlayerDetailDto dto = new PlayerDetailDto();
        dto.setPlayerId(player.getPlayerId());
        dto.setPlayerMoney(player.getPlayerMoney());
        dto.setHoldings(holdings);
        return dto;
    }
}

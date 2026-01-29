package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Player {

	@Id
	private String playerId;

	private String playerPassword;

	private double playerMoney;

	public Player(String id, double money) {
		this.playerId = id;
		this.playerMoney = money;
	}
}

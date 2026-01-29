package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Player {

	@Id
	@NotBlank(message = "playerId는 필수입니다")
	private String playerId;
	
	@NotBlank(message = "playerPassword는 필수입니다")
	private String playerPassword;
	
	@NotNull(message = "playerMoney는 필수입니다")
	private Double playerMoney;

	public Player(String playerPassword, Double playerMoney) {
		this.playerId = java.util.UUID.randomUUID().toString();
		this.playerPassword = playerPassword;
		this.playerMoney = playerMoney;
	}

}

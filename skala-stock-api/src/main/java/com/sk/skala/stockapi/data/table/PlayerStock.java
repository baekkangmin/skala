package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlayerStock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "player_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Player player;
	
	@JoinColumn(name = "stock_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Stock stock;
	
	private Long quantity;

	public PlayerStock(Player player, Stock stock, Long quantity) {
		this.player = player;
		this.stock = stock;
		this.quantity = quantity;
	}

}
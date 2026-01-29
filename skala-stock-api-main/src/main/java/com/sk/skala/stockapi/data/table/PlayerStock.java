package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class PlayerStock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id") // Player 테이블의 PK를 참조
	private Player player;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id") // Stock 테이블의 PK를 참조
	private Stock stock;

	private int quantity; // 보유 수량

	public PlayerStock(Player player, Stock stock, int quantity) {
		this.player = player;
		this.stock = stock;
		this.quantity = quantity;
	}
}
package com.sk.skala.stockapi.data.table;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String stockName;
	private Double stockPrice;

	public Stock() {
	}

	public Stock(String name, Double price) {
		this.stockName = name;
		this.stockPrice = price;
	}
}

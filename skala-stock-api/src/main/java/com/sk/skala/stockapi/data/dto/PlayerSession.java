package com.sk.skala.stockapi.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlayerSession {
	@NotBlank(message = "playerId는 필수입니다")
	private String playerId;
	
	@NotBlank(message = "playerPassword는 필수입니다")
	private String playerPassword;
}

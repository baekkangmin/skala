package com.sk.skala.stockapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "application")
@Data
public class ApplicationProperties {
	private String name;
	private Health health;

	@Data
	public static class Health {
		String url;
		long timeout;
	}
}

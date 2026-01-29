package com.sk.skala.stockapi.tools;

import java.util.Date;

import com.sk.skala.stockapi.config.Constant;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtTool {

	public static String generateToken(String id, Object payload) {
		long currentTimeMillis = System.currentTimeMillis();
		log.debug("JwtTool.generateToken: {} {}", id, payload.toString());
		return Jwts.builder().setIssuer(Constant.JWT_ISSUER).setId(id).setSubject(Constant.JWT_SUBJECT)
				.setIssuedAt(new Date(currentTimeMillis))
				.setExpiration(new Date(currentTimeMillis + Constant.JWT_TTL_MILLIS))
				.setAudience(JsonTool.toString(payload)).signWith(Keys.hmacShaKeyFor(Constant.JWT_SECRET.getBytes()))
				.compact();

	}

	public static String getValidPayload(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(Constant.JWT_SECRET.getBytes()).build().parseClaimsJws(token)
				.getBody();

		log.debug("JwtTool.getValidPayload: {} {}", claims.getIssuer(), claims.getAudience());
		return claims.getAudience();
	}
}

package com.sk.skala.stockapi.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sk.skala.stockapi.config.Constant;
import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.tools.JsonTool;
import com.sk.skala.stockapi.tools.JwtTool;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class SessionHandler {

	public PlayerSession getPlayerSession() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (Constant.JWT_ACCESS_COOKIE.equals(cookie.getName())) {
					String payload = JwtTool.getValidPayload(cookie.getValue());
					return JsonTool.toObject(payload, PlayerSession.class);
				}
			}
		}
		throw new ResponseException(Error.SESSION_NOT_FOUND);
	}

	public String getPlayerId() {
		PlayerSession playerSession = getPlayerSession();
		return playerSession != null ? playerSession.getPlayerId() : null;
	}

	public PlayerSession storeAccessToken(PlayerSession playerSession) {
		// hide password
		playerSession.setPlayerPassword("");

		String token = JwtTool.generateToken(playerSession.getPlayerId(), playerSession);
		Cookie cookie = new Cookie(Constant.JWT_ACCESS_COOKIE, token);
		cookie.setMaxAge(Constant.JWT_ACCESS_TTL);
		cookie.setPath("/");
		cookie.setSecure(false);

		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		attr.getResponse().addCookie(cookie);

		return playerSession;
	}

}
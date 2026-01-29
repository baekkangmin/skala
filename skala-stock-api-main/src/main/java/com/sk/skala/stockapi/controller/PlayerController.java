package com.sk.skala.stockapi.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.data.dto.StockOrder;
import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.service.PlayerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players")
public class PlayerController {

	private final PlayerService playerService;

	@GetMapping("/list")
	public Response getAllPlayers(@RequestParam(value = "offset", defaultValue = "0") int offset,
			@RequestParam(value = "count", defaultValue = "10") int count) {
		return playerService.getAllPlayers(offset, count);
	}

	@GetMapping("/{playerId}")
	public Response getPlayerById(@PathVariable String playerId) {
		return playerService.getPlayerById(playerId);
	}

	@PostMapping
	public Response createPlayer(@RequestBody Player player) {
		return playerService.createPlayer(player);
	}

	@PostMapping("/login")
	public Response loginPlayer(@RequestBody PlayerSession playerSession) {
		return playerService.loginPlayer(playerSession);
	}

	@PutMapping
	public Response updatePlayer(@RequestBody Player player) {
		return playerService.updatePlayer(player);
	}

	@DeleteMapping
	public Response deletePlayer(@RequestBody Player player) {
		return playerService.deletePlayer(player);
	}

	@PostMapping("/buy")
	public Response buyPlayerStock(@RequestBody StockOrder order) {
		return playerService.buyPlayerStock(order);
	}

	@PostMapping("/sell")
	public Response sellPlayerStock(@RequestBody StockOrder order) {
		return playerService.sellPlayerStock(order);
	}
}

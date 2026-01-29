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

    // 전체 플레이어 목록 조회 API
    @GetMapping("/list")
    public Response getAllPlayers(@RequestParam(defaultValue = "0") int offset,
                                  @RequestParam(defaultValue = "10") int count) {
        return playerService.getAllPlayers(offset, count);
    }

    // 단일 플레이어 상세 조회 API: 해당 플레이어 정보 및 보유 주식 목록 조회
    @GetMapping("/{playerId}")
    public Response getPlayerById(@PathVariable String playerId) {
        return playerService.getPlayerById(playerId);
    }

    // 플레이어 등록
    @PostMapping
    public Response createPlayer(@RequestBody Player player) {
        return playerService.createPlayer(player);  
    }

    // 플레이어 로그인
    @PostMapping("/login")
    public Response loginPlayer(@RequestBody PlayerSession playerSession) {
        return playerService.loginPlayer(playerSession);
    }

    // 플레이어 정보 수정
    @PutMapping
    public Response updatePlayer(@RequestBody Player player) {
        return playerService.updatePlayer(player);
    }

    // 플레이어 삭제
    @DeleteMapping("/{playerId}")
    public Response deletePlayer(@PathVariable String playerId) {
        return playerService.deletePlayer(playerId);
    }

    // 플레이어 주식 매수
    @PostMapping("/buy")
    public Response buyPlayerStock(@RequestBody StockOrder stockOrder) {
        return playerService.buyPlayerStock(stockOrder);
    }   

    // 플레이어 주식 매도
    @PostMapping("/sell")
    public Response sellPlayerStock(@RequestBody StockOrder stockOrder) {
        return playerService.sellPlayerStock(stockOrder);
    }
}

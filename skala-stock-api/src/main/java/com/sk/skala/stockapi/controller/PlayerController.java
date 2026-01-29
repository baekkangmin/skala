package com.sk.skala.stockapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Player", description = "플레이어 및 거래 관리API")
public class PlayerController {

    private final PlayerService playerService;

    // 전체 플레이어 목록 조회 API
    @Operation(summary = "플레이어 목록 조회", description = "전체 플레이어 목록을 페이징하여 조회합니다")
    @GetMapping("/list")
    public Response getAllPlayers(
            @Parameter(description = "페이지 오프셋", example = "0") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int count) {
        return playerService.getAllPlayers(offset, count);
    }

    // 단일 플레이어 상세 조회 API: 해당 플레이어 정보 및 보유 주식 목록 조회
    @Operation(summary = "플레이어 상세 조회", description = "특정 플레이어의 정보와 보유 주식 목록을 조회합니다")
    @GetMapping("/{playerId}")
    public Response getPlayerById(@Parameter(description = "플레이어 ID", example = "player1") @PathVariable String playerId) {
        return playerService.getPlayerById(playerId);
    }

    // 플레이어 등록
    @Operation(summary = "플레이어 회원가입", description = "새로운 플레이어를 등록합니다")
    @PostMapping
    public Response createPlayer(@Valid @RequestBody Player player) {
        return playerService.createPlayer(player);  
    }

    // 플레이어 로그인
    @Operation(summary = "플레이어 로그인", description = "플레이어 로그인을 처리하고 세션을 생성합니다")
    @PostMapping("/login")
    public Response loginPlayer(@Valid @RequestBody PlayerSession playerSession) {
        return playerService.loginPlayer(playerSession);
    }

    // 플레이어 정보 수정
    @Operation(summary = "플레이어 정보 수정", description = "플레이어의 자산 등 정보를 수정합니다")
    @PutMapping
    public Response updatePlayer(@Valid @RequestBody Player player) {
        return playerService.updatePlayer(player);
    }

    // 플레이어 삭제
    @Operation(summary = "플레이어 삭제", description = "특정 플레이어를 삭제합니다")
    @DeleteMapping("/{playerId}")
    public Response deletePlayer(@Parameter(description = "플레이어 ID", example = "player1") @PathVariable String playerId) {
        return playerService.deletePlayer(playerId);
    }

    // 플레이어 주식 매수
    @Operation(summary = "주식 매수", description = "플레이어가 주식을 매수합니다")
    @PostMapping("/buy")
    public Response buyPlayerStock(@Valid @RequestBody StockOrder stockOrder) {
        return playerService.buyPlayerStock(stockOrder);
    }   

    // 플레이어 주식 매도
    @Operation(summary = "주식 매도", description = "플레이어가 보유 주식을 매도합니다")
    @PostMapping("/sell")
    public Response sellPlayerStock(@Valid @RequestBody StockOrder stockOrder) {
        return playerService.sellPlayerStock(stockOrder);
    }
}

package com.sk.skala.stockapi.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.data.table.PlayerStock;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.data.dto.PlayerDetailDto;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.data.dto.PlayerStockDto;
import com.sk.skala.stockapi.data.dto.StockOrder;

import java.util.HashMap;
import java.util.Map;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.PlayerRepository;
import com.sk.skala.stockapi.repository.PlayerStockRepository;
import com.sk.skala.stockapi.repository.StockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final StockRepository stockRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStockRepository playerStockRepository;
    private final SessionHandler sessionHandler;

    // 전체 플레이어 목록 조회
    public Response getAllPlayers(int offset, int count) {
        validatePaging(offset, count);

        Pageable pageable = PageRequest.of(offset, count);
        Page<Player> page = playerRepository.findAll(pageable);

        Object pagedList = toPagedList(page, offset, count);

        return Response.ok(pagedList);

    }

    // 단일 플레이어 및 주식 목록
    @Transactional
    public Response getPlayerById(String playerId) {
        if(isBlank(playerId)) {
            throw new ParameterException("playerId");
        }       

        Player player = playerRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Player not found"));
        
        List<PlayerStock> holdings = playerStockRepository.findByPlayer_PlayerId(playerId);

        // Stream API로 DTO 리스트 변환
        List<PlayerStockDto> holdingDtos = holdings.stream()
                .map(ps -> PlayerStockDto.from(ps))
                .collect(Collectors.toList());
        
        PlayerDetailDto body = PlayerDetailDto.from(player, holdingDtos);
        return Response.ok(body);

    }

    // 플레이어 생성
    public Response createPlayer(Player player) {
        if (player == null) {
            throw new ParameterException("playerId", "playerPassword");
        }

        String playerId = player.getPlayerId();
        String playerPassword = player.getPlayerPassword();
        Double playerMoney = player.getPlayerMoney();

        // 초기 자산 유효성 검사
        if (playerMoney == null || playerMoney <= 0) {
            throw new ParameterException("playerMoney");
        }

        // 중복 아이디 체크
        if (playerRepository.existsByPlayerId(playerId)) {
            throw new ResponseException(Error.DATA_DUPLICATED);
        }

        // Player 객체 생성 + 초기 자산 세팅
        Player newPlayer = new Player();
        newPlayer.setPlayerId(playerId);
        newPlayer.setPlayerPassword(playerPassword);
        newPlayer.setPlayerMoney(playerMoney);

        Player saved = playerRepository.save(newPlayer);

        // 비밀번호는 응답에서 제거
        saved.setPlayerPassword(null);
        return Response.ok(saved);

        
    }

    // 플레이어 로그인
    public Response loginPlayer(PlayerSession playerSession) {
        if (playerSession == null) {
            throw new ParameterException("playerId", "playerPassword");
        }

        String playerId = playerSession.getPlayerId();
        String playerPassword = playerSession.getPlayerPassword();

        Player player = playerRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        // 비밀번호 검증
        if (!Objects.equals(player.getPlayerPassword(), playerPassword)) {
            throw new ResponseException(Error.NOT_AUTHENTICATED);
        }

        // 인증 성공 시 토큰 저장
        sessionHandler.storeAccessToken(playerSession);

        // 비밀번호 null 처리 후 응답
        player.setPlayerPassword(null);
        return Response.ok(player);

    }

    // 플레이어 정보 업데이트
    public Response updatePlayer(Player player) {
        if (player == null || isBlank(player.getPlayerId())) {
            throw new ParameterException("playerId");
        }

        if (player.getPlayerMoney() == null || player.getPlayerMoney() < 0) {
            throw new ParameterException("playerMoney");
        }   

        Player existing = playerRepository.findByPlayerId(player.getPlayerId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        // 자산 업데이트
        existing.setPlayerMoney(player.getPlayerMoney());

        Player saved = playerRepository.save(existing);
        saved.setPlayerPassword(null);

        return Response.ok(saved);

    }

    // 플레이어 삭제
    public Response deletePlayer(String playerId) {
        if (isBlank(playerId)) {
            throw new ParameterException("playerId");
        }

        Player existing = playerRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        playerRepository.delete(existing);

        return Response.ok(true);
    }

    // 주식 매수
    @Transactional
    public Response buyPlayerStock(StockOrder order) {
        validateOrder(order);

        // 현재 로그인된 playerId 가져오기
        String playerId = sessionHandler.getPlayerId()
                .orElseThrow(() -> new ResponseException(Error.NOT_AUTHENTICATED));

        Player player = playerRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        Stock stock = stockRepository.findById(order.getStockId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        long qty = order.getQuantity();
        long totalCost = stock.getStockPrice().longValue() * qty;

        // 잔액 부족 체크
        if (player.getPlayerMoney() < totalCost) {
            throw new ResponseException(Error.INSUFFICIENT_FUNDS);
        }

        // 플레이어 자산 차감
        player.setPlayerMoney(player.getPlayerMoney() - totalCost);

        // 보유 주식이면 수량 추가, 없으면 신규 생성
        PlayerStock playerStock = playerStockRepository.findByPlayerAndStock(player, stock)
                .orElseGet(() -> {
                    PlayerStock ps = new PlayerStock();
                    ps.setPlayer(player);
                    ps.setStock(stock);
                    ps.setQuantity(0L);
                    return ps;
                });

                playerStock.setQuantity(playerStock.getQuantity() + qty);

        // 변경사항 저장
        playerRepository.save(player);
        playerStockRepository.save(playerStock);

        return Response.ok(true);
    }

    // 주식 매도
    @Transactional
    public Response sellPlayerStock(StockOrder order) {
        validateOrder(order);

        // 현재 로그인된 playerId 가져오기
        String playerId = sessionHandler.getPlayerId()
                .orElseThrow(() -> new ResponseException(Error.NOT_AUTHENTICATED));

        Player player = playerRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        Stock stock = stockRepository.findById(order.getStockId())
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        PlayerStock playerStock = playerStockRepository.findByPlayerAndStock(player, stock)
                .orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND));

        long qty = order.getQuantity();


        // 보유 수량 검증
        if (playerStock.getQuantity() < qty) {
            throw new ResponseException(Error.INSUFFICIENT_STOCK_QUANTITY);
        }   

        // 수량 감소 혹은 삭제
        long remain = playerStock.getQuantity() - qty;
        if (remain == 0){
            playerStockRepository.delete(playerStock);
        } else {
            playerStock.setQuantity(remain);
            playerStockRepository.save(playerStock);
        }

        // 매도 금액만큼 자산 증가
        long proceeds = stock.getStockPrice().longValue() * qty;
        player.setPlayerMoney(player.getPlayerMoney() + proceeds);
        playerRepository.save(player);
        return Response.ok(true);
    }

    // Helper methods
    private void validatePaging(int offset, int count) {
        if (offset < 0) {
            throw new ParameterException("offset");
        }
        if (count <= 0) {
            throw new ParameterException("count");
        }
    }

    private void validateOrder(StockOrder order) {
        if (order == null) {
            throw new ParameterException("order");
        }
        if (order.getStockId() == null) {
            throw new ParameterException("stockId");
        }
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            throw new ParameterException("quantity");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private Object toPagedList(Page<?> page, int offset, int count) {
        Map<String, Object> result = new HashMap<>();
        result.put("content", page.getContent());
        result.put("totalElements", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());
        result.put("currentPage", offset);
        result.put("pageSize", count);
        result.put("hasNext", page.hasNext());
        result.put("hasPrevious", page.hasPrevious());
        return result;
    }
}
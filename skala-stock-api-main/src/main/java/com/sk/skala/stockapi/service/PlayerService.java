package com.sk.skala.stockapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.common.PagedList;
import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.data.dto.PlayerSession;
import com.sk.skala.stockapi.data.dto.PlayerStockDto;
import com.sk.skala.stockapi.data.dto.PlayerStockListDto;
import com.sk.skala.stockapi.data.dto.StockOrder;
import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.data.table.PlayerStock;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.PlayerRepository;
import com.sk.skala.stockapi.repository.PlayerStockRepository;
import com.sk.skala.stockapi.repository.StockRepository;
import com.sk.skala.stockapi.tools.StringTool;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

	private final StockRepository stockRepository;
	private final PlayerRepository playerRepository;
	private final PlayerStockRepository playerStockRepository;
	private final SessionHandler sessionHandler;

	public Response getAllPlayers(int offset, int count) {
		Pageable pageable = PageRequest.of(offset, count, Sort.by(Sort.Order.asc("playerId")));
		Page<Player> paged = playerRepository.findAll(pageable);

		PagedList pagedList = new PagedList();
		pagedList.setTotal(paged.getTotalElements());
		pagedList.setOffset(offset);
		pagedList.setCount(paged.getNumberOfElements());
		pagedList.setList(paged.getContent());

		Response response = new Response();
		response.setBody(pagedList);
		return response;
	}

	@Transactional(readOnly = true)
	public Response getPlayerById(String playerId) {
		// 1. 플레이어 정보 조회. 없으면 예외 발생.
		Player player = playerRepository.findById(playerId)
				.orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Player not found"));

		// 2. 해당 플레이어가 보유한 주식 목록(PlayerStock)을 조회.
		List<PlayerStock> playerStocks = playerStockRepository.findByPlayer_PlayerId(playerId);

		// 3. 조회한 'PlayerStock' 엔터티 리스트를 'PlayerStockDto' 리스트로 변환.
		// Java Stream API를 사용하여 간결하게 매핑합니다.
		List<PlayerStockDto> stockDtos = playerStocks.stream()
				.map(playerStock -> PlayerStockDto.builder().stockId(playerStock.getStock().getId())
						.stockName(playerStock.getStock().getStockName())
						.stockPrice(playerStock.getStock().getStockPrice()).quantity(playerStock.getQuantity()).build())
				.collect(Collectors.toList());

		// 4. 최종적으로 반환할 PlayerDetailDto 객체를 생성.
		PlayerStockListDto playerStockListDto = PlayerStockListDto.builder().playerId(player.getPlayerId())
				.playerMoney(player.getPlayerMoney()).stocks(stockDtos) // 변환된 주식 DTO 리스트를 설정
				.build();

		// 5. 완성된 DTO를 Response 객체에 담아 반환.
		Response response = new Response();
		response.setBody(playerStockListDto);
		return response;
	}

	public Response createPlayer(Player playerSession) {
		if (StringTool.isAnyEmpty(playerSession.getPlayerId())
				|| StringTool.isAnyEmpty(playerSession.getPlayerPassword())) {
			throw new ParameterException("playerId", "playerPassword");
		}

		Optional<Player> option = playerRepository.findById(playerSession.getPlayerId());
		if (!option.isEmpty()) {
			throw new ResponseException(Error.DATA_DUPLICATED);
		}
		Player player = new Player();
		player.setPlayerId(playerSession.getPlayerId());
		player.setPlayerPassword(playerSession.getPlayerPassword());
		player.setPlayerMoney(100000);

		playerRepository.save(player);

		return new Response();
	}

	public Response loginPlayer(PlayerSession playerSession) {
		if (StringTool.isAnyEmpty(playerSession.getPlayerId())
				|| StringTool.isAnyEmpty(playerSession.getPlayerPassword())) {
			throw new ParameterException("playerId", "playerPassword");
		}

		Optional<Player> option = playerRepository.findById(playerSession.getPlayerId());
		if (option.isEmpty()) {
			throw new ResponseException(Error.DATA_NOT_FOUND);
		}

		Player player = option.get();
		if (player.getPlayerPassword().equals(playerSession.getPlayerPassword())) {
			sessionHandler.storeAccessToken(playerSession);
		} else {
			throw new ResponseException(Error.NOT_AUTHENTICATED);
		}

		player.setPlayerPassword(null);
		Response response = new Response();
		response.setBody(player);
		return response;
	}

	public Response updatePlayer(Player player) {
		if (StringTool.isAnyEmpty(player.getPlayerId()) || player.getPlayerMoney() <= 0) {
			throw new ParameterException("playerId", "playerMoney");
		}

		Optional<Player> option = playerRepository.findById(player.getPlayerId());
		if (option.isEmpty()) {
			throw new ResponseException(Error.DATA_NOT_FOUND);
		}
		Player storedPlayer = option.get();
		storedPlayer.setPlayerMoney(player.getPlayerMoney());
		playerRepository.save(storedPlayer);

		return new Response();
	}

	public Response deletePlayer(Player player) {
		Optional<Player> option = playerRepository.findById(player.getPlayerId());
		if (option.isEmpty()) {
			throw new ResponseException(Error.DATA_NOT_FOUND);
		}
		playerRepository.deleteById(player.getPlayerId());
		return new Response();
	}

	@Transactional
	public Response buyPlayerStock(StockOrder order) {
		String playerId = sessionHandler.getPlayerId();

		// 1. 플레이어와 주식 엔터티 조회
		Player player = playerRepository.findById(playerId)
				.orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Player not found"));

		Stock stock = stockRepository.findById(order.getStockId())
				.orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Stock not found"));

		// 2. 금액 확인 및 차감
		double totalCost = stock.getStockPrice() * order.getStockQuantity();
		if (totalCost > player.getPlayerMoney()) {
			throw new ResponseException(Error.INSUFFICIENT_FUNDS);
		}
		player.setPlayerMoney(player.getPlayerMoney() - totalCost);
		playerRepository.save(player);

		// 3. PlayerStockRepository를 이용해 기존 보유 내역 조회
		Optional<PlayerStock> optionalPlayerStock = playerStockRepository.findByPlayerAndStock(player, stock);

		if (optionalPlayerStock.isPresent()) {
			// 4-1. 이미 보유한 주식이면 수량만 추가
			PlayerStock existingPlayerStock = optionalPlayerStock.get();
			existingPlayerStock.setQuantity(existingPlayerStock.getQuantity() + order.getStockQuantity());
			playerStockRepository.save(existingPlayerStock);
		} else {
			// 4-2. 처음 매수하는 주식이면 새로운 PlayerStock 생성
			PlayerStock newPlayerStock = new PlayerStock(player, stock, order.getStockQuantity());
			playerStockRepository.save(newPlayerStock);
		}

		return new Response();
	}

	@Transactional
	public Response sellPlayerStock(StockOrder order) {
		String playerId = sessionHandler.getPlayerId();

		// 1. 플레이어와 주식 엔터티 조회
		Player player = playerRepository.findById(playerId)
				.orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Player not found"));

		Stock stock = stockRepository.findById(order.getStockId())
				.orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Stock not found"));

		// 2. PlayerStockRepository를 이용해 매도할 주식 보유 내역 조회
		PlayerStock playerStock = playerStockRepository.findByPlayerAndStock(player, stock)
				.orElseThrow(() -> new ResponseException(Error.DATA_NOT_FOUND, "Player does not own this stock"));

		// 3. 매도 수량 확인
		if (order.getStockQuantity() > playerStock.getQuantity()) {
			throw new ResponseException(Error.INSUFFICIENT_QUANTITY);
		}

		// 4. 수량 변경 또는 보유 내역 삭제
		int newQuantity = playerStock.getQuantity() - order.getStockQuantity();
		if (newQuantity == 0) {
			// 전부 매도한 경우, 보유 내역 삭제
			playerStockRepository.delete(playerStock);
		} else {
			// 일부만 매도한 경우, 수량 업데이트
			playerStock.setQuantity(newQuantity);
			playerStockRepository.save(playerStock);
		}

		// 5. 플레이어 자산 증가
		double totalEarnings = stock.getStockPrice() * order.getStockQuantity();
		player.setPlayerMoney(player.getPlayerMoney() + totalEarnings);
		playerRepository.save(player);

		return new Response();
	}
}

package com.sk.skala.stockapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sk.skala.stockapi.data.table.Player;
import com.sk.skala.stockapi.data.table.PlayerStock;
import com.sk.skala.stockapi.data.table.Stock;

public interface PlayerStockRepository extends JpaRepository<PlayerStock, Long> {

    List<PlayerStock> findByPlayer_PlayerId(String playerId);
    Optional<PlayerStock> findByPlayerAndStock(Player player, Stock stock);

}
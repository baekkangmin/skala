package com.sk.skala.stockapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sk.skala.stockapi.data.table.Player;

public interface PlayerRepository extends JpaRepository<Player, String> {
    Optional<Player> findByPlayerId(String playerId);
    boolean existsByPlayerId(String playerId);
}

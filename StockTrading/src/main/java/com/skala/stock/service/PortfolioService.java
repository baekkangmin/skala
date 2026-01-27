package com.skala.stock.service;

import com.skala.stock.dto.PortfolioDto;
import com.skala.stock.entity.Portfolio;
import com.skala.stock.entity.Stock;
import com.skala.stock.entity.User;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public List<PortfolioDto> getUserPortfolio(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        return portfolios.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PortfolioDto getPortfolio(Long userId, Long stockId) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));
        return convertToDto(portfolio);
    }

    @Transactional
    public PortfolioDto addToPortfolio(Long userId, Long stockId, Long quantity, Long price) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + stockId));

        Portfolio existingPortfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElse(null);

        if (existingPortfolio != null) {
            // 기존 포트폴리오가 있으면 평균 단가 재계산
            Long totalQuantity = existingPortfolio.getQuantity() + quantity;
            Long totalCost = (existingPortfolio.getAveragePrice() * existingPortfolio.getQuantity()) + (price * quantity);
            // 정수 나눗셈 방지: Double로 계산 후 반올림
            Long newAveragePrice = Math.round((double) totalCost / totalQuantity);

            existingPortfolio.setQuantity(totalQuantity);
            existingPortfolio.setAveragePrice(newAveragePrice);
            Portfolio updated = portfolioRepository.save(existingPortfolio);
            return convertToDto(updated);
        } else {
            // 새로운 포트폴리오 생성
            Portfolio newPortfolio = Portfolio.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(quantity)
                    .averagePrice(price)
                    .build();
            Portfolio saved = portfolioRepository.save(newPortfolio);
            return convertToDto(saved);
        }
    }

    @Transactional
    public PortfolioDto updatePortfolio(Long userId, Long stockId, Long quantity) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));

        if (quantity <= 0) {
            // 수량이 0 이하면 포트폴리오 삭제하고 빈 DTO 반환
            portfolioRepository.delete(portfolio);
            throw new RuntimeException("수량은 0보다 커야 합니다. 포트폴리오를 삭제하려면 removeFromPortfolio 메서드를 사용하세요.");
        }

        portfolio.setQuantity(quantity);
        Portfolio updated = portfolioRepository.save(portfolio);
        return convertToDto(updated);
    }

    @Transactional
    public void removeFromPortfolio(Long userId, Long stockId) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));
        portfolioRepository.delete(portfolio);
    }

    private PortfolioDto convertToDto(Portfolio portfolio) {
        Stock stock = portfolio.getStock();
        Long currentPrice = stock.getCurrentPrice();
        Long totalValue = portfolio.getQuantity() * currentPrice;
        Long profitLoss = totalValue - (portfolio.getQuantity() * portfolio.getAveragePrice());

        return PortfolioDto.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUser().getId())
                .username(portfolio.getUser().getUsername())
                .stockId(stock.getId())
                .stockCode(stock.getCode())
                .stockName(stock.getName())
                .quantity(portfolio.getQuantity())
                .averagePrice(portfolio.getAveragePrice())
                .currentPrice(currentPrice)
                .totalValue(totalValue)
                .profitLoss(profitLoss)
                .build();
    }
}

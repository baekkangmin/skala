package com.skala.stock.service;

import com.skala.stock.dto.StockDto;
import com.skala.stock.entity.Stock;
import com.skala.stock.repository.PortfolioRepository;
import com.skala.stock.repository.StockRepository;
import com.skala.stock.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional // 전부 성공하면 커밋, 하나라도 실패하면 롤백. DB 상태를 일관성 있게 유지
    public StockDto createStock(StockDto stockDto) {
        if (stockRepository.existsByCode(stockDto.getCode())) {
            throw new RuntimeException("이미 존재하는 종목 코드입니다: " + stockDto.getCode());
        }

        Stock stock = Stock.builder()
                .code(stockDto.getCode())
                .name(stockDto.getName())
                .currentPrice(stockDto.getCurrentPrice())
                .previousPrice(stockDto.getPreviousPrice())
                .build();

        Stock savedStock = stockRepository.save(stock);
        return convertToDto(savedStock);
    }

    public StockDto getStockById(Long id) {
        Stock stock = getStockEntity(id);
        return convertToDto(stock);
    }

    public List<StockDto> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ 추가: 코드로 조회
    public StockDto getStockByCode(String code) {
        Stock stock = stockRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다. code=" + code));
        return convertToDto(stock);
    }

    // ✅ 추가: 업데이트
    @Transactional
    public StockDto updateStock(Long id, StockDto stockDto) {
        Stock stock = getStockEntity(id);

        // code 변경 시 중복 체크 (자기 자신 제외)
        String newCode = stockDto.getCode();
        if (newCode != null && !newCode.equals(stock.getCode())) {
            if (stockRepository.existsByCodeAndIdNot(newCode, id)) {
                throw new RuntimeException("이미 존재하는 종목 코드입니다: " + newCode);
            }
            stock.setCode(newCode);
        }

        if (stockDto.getName() != null) {
            stock.setName(stockDto.getName());
        }
        if (stockDto.getCurrentPrice() != null) {
            stock.setCurrentPrice(stockDto.getCurrentPrice());
        }
        stock.setPreviousPrice(stockDto.getPreviousPrice()); // null 허용이면 그대로 반영

        Stock saved = stockRepository.save(stock);
        return convertToDto(saved);
    }

    // ✅ 추가: 삭제 (참조 있으면 삭제 불가)
    @Transactional
    public void deleteStock(Long id) {
        // 존재 확인
        getStockEntity(id);

        // 거래/포트폴리오에서 참조 중이면 삭제 불가 (실습용 정책)
        if (transactionRepository.existsByStockId(id) || portfolioRepository.existsByStockId(id)) {
            throw new RuntimeException("거래/포트폴리오에 참조된 주식은 삭제할 수 없습니다. stockId=" + id);
        }

        stockRepository.deleteById(id);
    }

    private Stock getStockEntity(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("주식을 찾을 수 없습니다: " + id));
    }

    private StockDto convertToDto(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .code(stock.getCode())
                .name(stock.getName())
                .currentPrice(stock.getCurrentPrice())
                .previousPrice(stock.getPreviousPrice())
                .build();
    }
}

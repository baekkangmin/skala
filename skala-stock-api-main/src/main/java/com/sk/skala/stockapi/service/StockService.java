package com.sk.skala.stockapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.common.PagedList;
import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.data.table.Stock;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;
import com.sk.skala.stockapi.repository.StockRepository;
import com.sk.skala.stockapi.tools.StringTool;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockService {

	private final StockRepository stockRepository;

	public Response getAllStocks(int offset, int count) {
		Pageable pageable = PageRequest.of(offset, count, Sort.by(Sort.Order.asc("id")));
		Page<Stock> paged = stockRepository.findAll(pageable);

		PagedList pagedList = new PagedList();
		pagedList.setTotal(paged.getTotalElements());
		pagedList.setOffset(offset);
		pagedList.setCount(paged.getNumberOfElements());
		pagedList.setList(paged.getContent());

		Response response = new Response();
		response.setBody(pagedList);
		return response;
	}

	public Response getStockById(Long id) {
		Optional<Stock> option = stockRepository.findById(id);
		if (option.isEmpty()) {
			throw new ResponseException(Error.DATA_NOT_FOUND);
		}

		Response response = new Response();
		response.setBody(option.get());
		return response;
	}

	public Response createStock(Stock stock) {
		if (StringTool.isAnyEmpty(stock.getStockName()) || stock.getStockPrice() <= 0) {
			throw new ParameterException("stockName", "stockPrice");
		}

		Optional<Stock> option = stockRepository.findByStockName(stock.getStockName());
		if (option.isPresent()) {
			throw new ResponseException(Error.DATA_DUPLICATED);
		}

		stock.setId(0L);
		stockRepository.save(stock);

		return new Response();
	}

	public Response updateStock(Stock stock) {
		if (StringTool.isAnyEmpty(stock.getStockName()) || stock.getStockPrice() <= 0) {
			throw new ParameterException("stockName", "stockPrice");
		}

		Optional<Stock> option = stockRepository.findById(stock.getId());
		if (option.isEmpty()) {
			throw new ResponseException(Error.DATA_NOT_FOUND);
		}
		stockRepository.save(stock);
		return new Response();
	}

	public Response deleteStock(Stock stock) {
		Optional<Stock> option = stockRepository.findById(stock.getId());
		if (option.isEmpty()) {
			throw new ResponseException(Error.DATA_NOT_FOUND);
		}
		stockRepository.deleteById(stock.getId());
		return new Response();
	}
}

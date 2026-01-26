package com.example.product.controller;

import com.example.product.dto.OrderCreateRequest;
import com.example.product.dto.OrderResponse;
import com.example.product.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    // 장바구니 기반 주문 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid OrderCreateRequest r) {
        return orderService.createOrderFromCart(r.getMemberId());
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable @Min(1) Long orderId) {
        return orderService.getOrder(orderId);
    }

    // 환불/취소(간단 버전)
    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable @Min(1) Long orderId) {
        orderService.cancelOrder(orderId);
    }
}

package com.example.product.service;

import com.example.product.domain.*;
import com.example.product.dto.OrderItemResponse;
import com.example.product.dto.OrderResponse;
import com.example.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final MemberMapper memberMapper;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderResponse createOrderFromCart(Long memberId) {
        // member 존재 확인
        memberMapper.findById(memberId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "member not found"));

        // cart 조회
        Cart cart = cartMapper.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "cart not found"));

        List<CartItemView> cartItems = cartItemMapper.findViewsByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "cart is empty");
        }

        // 1) 주문 생성 (CREATED)
        Order order = new Order();
        order.setMemberId(memberId);
        order.setStatus("CREATED");
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        BigDecimal total = BigDecimal.ZERO;

        // 2) 재고 차감 + order_items 생성
        for (CartItemView ci : cartItems) {
            int updated = productMapper.decreaseStock(ci.getProductId(), ci.getQuantity());
            if (updated == 0) {
                // 조건부 차감 실패 => 재고 부족 (트랜잭션이라 여기서 던지면 전체 롤백)
                throw new ResponseStatusException(CONFLICT, "out of stock: productId=" + ci.getProductId());
            }

            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(ci.getProductId());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(ci.getLineTotal());
            orderItemMapper.insert(oi);

            total = total.add(ci.getLineTotal());
        }

        // 3) orders.total_amount 업데이트 (간단히 status update SQL만 있어서, 재조회로 응답 구성)
        // (깔끔하게 하려면 total_amount update 메서드를 따로 만들어도 됩니다.)
        // 여기서는 orders 테이블에 total_amount가 이미 들어갔으니, insert 전에 total 계산해서 넣는 방식도 가능.
        // 최소 변경으로: updateStatus만 있으니, order를 다시 조회 응답 구성할 때 total은 items 합으로 계산합니다.

        // 4) 장바구니 비우기
        cartItemMapper.deleteAllByCartId(cart.getId());

        return getOrder(order.getId());
    }

    public OrderResponse getOrder(Long orderId) {
        Order order = orderMapper.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "order not found"));

        List<OrderItemView> views = orderItemMapper.findViewsByOrderId(orderId);

        List<OrderItemResponse> items = views.stream().map(v ->
                OrderItemResponse.builder()
                        .productId(v.getProductId())
                        .productName(v.getProductName())
                        .unitPrice(v.getUnitPrice())
                        .quantity(v.getQuantity())
                        .lineTotal(v.getLineTotal())
                        .build()
        ).toList();

        BigDecimal total = views.stream()
                .map(OrderItemView::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponse.builder()
                .orderId(order.getId())
                .memberId(order.getMemberId())
                .status(order.getStatus())
                .totalAmount(total)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "order not found"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(CONFLICT, "already cancelled");
        }

        // 주문 항목 조회 후 재고 복구
        List<OrderItem> items = orderItemMapper.findByOrderId(orderId);
        for (OrderItem oi : items) {
            productMapper.increaseStock(oi.getProductId(), oi.getQuantity());
        }

        // 주문 상태 변경
        orderMapper.updateStatus(orderId, "CANCELLED");
    }
}

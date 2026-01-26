package com.example.product.service;

import com.example.product.domain.*;
import com.example.product.dto.CartResponse;
import com.example.product.dto.CartItemResponse;
import com.example.product.mapper.CartItemMapper;
import com.example.product.mapper.CartMapper;
import com.example.product.mapper.MemberMapper;
import com.example.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final MemberMapper memberMapper;
    private final ProductMapper productMapper;

    @Transactional
    public void addItem(Long memberId, Long productId, Integer quantity) {
        // member 존재 체크
        memberMapper.findById(memberId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "member not found"));

        // product 존재 체크
        productMapper.findById(productId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "product not found"));

        Cart cart = cartMapper.findByMemberId(memberId).orElseGet(() -> {
            Cart c = new Cart();
            c.setMemberId(memberId);
            cartMapper.insert(c);
            return c;
        });

        CartItem item = cartItemMapper.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (item == null) {
            CartItem ni = new CartItem();
            ni.setCartId(cart.getId());
            ni.setProductId(productId);
            ni.setQuantity(quantity);
            cartItemMapper.insert(ni);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
            cartItemMapper.updateQty(item);
        }
    }

    public CartResponse getCart(Long memberId) {
        Cart cart = cartMapper.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "cart not found"));

        List<CartItemView> views = cartItemMapper.findViewsByCartId(cart.getId());
        List<CartItemResponse> items = views.stream().map(v ->
                CartItemResponse.builder()
                        .productId(v.getProductId())
                        .productName(v.getProductName())
                        .unitPrice(v.getUnitPrice())
                        .quantity(v.getQuantity())
                        .lineTotal(v.getLineTotal())
                        .build()
        ).toList();

        BigDecimal total = views.stream()
                .map(CartItemView::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .memberId(memberId)
                .items(items)
                .totalAmount(total)
                .build();
    }

    @Transactional
    public void updateQty(Long memberId, Long productId, Integer quantity) {
        Cart cart = cartMapper.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "cart not found"));

        CartItem item = cartItemMapper.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "cart item not found"));

        item.setQuantity(quantity);
        cartItemMapper.updateQty(item);
    }

    @Transactional
    public void removeItem(Long memberId, Long productId) {
        Cart cart = cartMapper.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "cart not found"));

        cartItemMapper.deleteByCartIdAndProductId(cart.getId(), productId);
    }
}
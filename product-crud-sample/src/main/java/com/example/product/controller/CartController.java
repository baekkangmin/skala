package com.example.product.controller;

import com.example.product.dto.CartAddRequest;
import com.example.product.dto.CartResponse;
import com.example.product.dto.CartUpdateQtyRequest;
import com.example.product.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody @Valid CartAddRequest r) {
        cartService.addItem(r.getMemberId(), r.getProductId(), r.getQuantity());
    }

    @GetMapping
    public CartResponse get(@RequestParam @Min(1) Long memberId) {
        return cartService.getCart(memberId);
    }

    @PutMapping("/items")
    public void updateQty(@RequestBody @Valid CartUpdateQtyRequest r) {
        cartService.updateQty(r.getMemberId(), r.getProductId(), r.getQuantity());
    }

    @DeleteMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestParam @Min(1) Long memberId,
                       @RequestParam @Min(1) Long productId) {
        cartService.removeItem(memberId, productId);
    }
}

package com.example.product.mapper;

import com.example.product.domain.CartItem;
import com.example.product.domain.CartItemView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CartItemMapper {
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    void insert(CartItem item);
    void updateQty(CartItem item);
    void deleteByCartIdAndProductId(Long cartId, Long productId);
    void deleteAllByCartId(Long cartId);

    List<CartItemView> findViewsByCartId(Long cartId);
}

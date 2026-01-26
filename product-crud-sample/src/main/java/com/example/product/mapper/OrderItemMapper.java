package com.example.product.mapper;

import com.example.product.domain.OrderItem;
import com.example.product.domain.OrderItemView;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    void insert(OrderItem item);
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItemView> findViewsByOrderId(Long orderId);
}

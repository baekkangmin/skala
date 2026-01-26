package com.example.product.mapper;
import com.example.product.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;
@Mapper
public interface ProductMapper{
 List<Product> findAll();
 Optional<Product> findById(Long id);
 void insert(Product p);
 void update(Product p);
 void deleteById(Long id);
 int decreaseStock(Long id, Integer qty);
 int increaseStock(Long id, Integer qty);

}
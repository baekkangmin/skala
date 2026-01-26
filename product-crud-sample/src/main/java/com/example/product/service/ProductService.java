package com.example.product.service;
import com.example.product.domain.Product;
import com.example.product.dto.*;
import com.example.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ProductService{
 private final ProductMapper mapper;

 public List<ProductResponse> getProducts(){
   return mapper.findAll().stream().map(this::toRes).toList();
 }
 public ProductResponse getProduct(Long id){
   Product p=mapper.findById(id).orElseThrow();
   return toRes(p);
 }
 @Transactional
 public ProductResponse createProduct(ProductRequest r){
   Product p=new Product();
   p.setName(r.getName());
   p.setDescription(r.getDescription());
   p.setPrice(r.getPrice());
   p.setStock(r.getStock());
   mapper.insert(p);
   return getProduct(p.getId());
 }
 @Transactional
 public ProductResponse updateProduct(Long id, ProductRequest r){
   Product p=mapper.findById(id).orElseThrow();
   p.setName(r.getName());
   p.setDescription(r.getDescription());
   p.setPrice(r.getPrice());
   p.setStock(r.getStock());
   mapper.update(p);
   return getProduct(id);
 }
 @Transactional
 public void deleteProduct(Long id){ mapper.deleteById(id); }

 private ProductResponse toRes(Product p){
   return ProductResponse.builder()
     .id(p.getId()).name(p.getName())
     .description(p.getDescription())
     .price(p.getPrice()).stock(p.getStock())
     .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
     .build();
 }
}
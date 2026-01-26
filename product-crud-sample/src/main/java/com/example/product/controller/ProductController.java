package com.example.product.controller;
import com.example.product.dto.*;
import com.example.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController{
 private final ProductService service;

 @GetMapping public List<ProductResponse> all(){ return service.getProducts();}
 @GetMapping("/{id}") public ProductResponse one(@PathVariable Long id){ return service.getProduct(id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED)
 public ProductResponse create(@RequestBody ProductRequest r){ return service.createProduct(r);}
 @PutMapping("/{id}") public ProductResponse update(@PathVariable Long id,@RequestBody ProductRequest r){
   return service.updateProduct(id,r);
 }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(@PathVariable Long id){ service.deleteProduct(id);}
}
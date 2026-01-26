package com.example.product.controller;

import com.example.product.dto.*;
import com.example.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated  
public class ProductController {
    private final ProductService service;

    @GetMapping
    public List<ProductResponse> all() {
        return service.getProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse one(@PathVariable @Min(value = 1, message = "ID는 1 이상이어야 합니다") Long id) {
        return service.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@RequestBody @Valid ProductRequest r) {
        return service.createProduct(r);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable @Min(value = 1, message = "ID는 1 이상이어야 합니다") Long id,
            @RequestBody @Valid ProductRequest r
    ) {
        return service.updateProduct(id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Min(value = 1, message = "ID는 1 이상이어야 합니다") Long id) {
        service.deleteProduct(id);
    }
}
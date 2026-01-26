package com.example.product.domain;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class Product{
 private Long id;
 private String name;
 private String description;
 private BigDecimal price;
 private Integer stock;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;
// New fields
private String category; 
private String status;   
}
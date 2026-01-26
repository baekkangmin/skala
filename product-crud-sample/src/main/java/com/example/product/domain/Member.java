package com.example.product.domain;
import java.time.LocalDateTime;

import lombok.Data;
@Data
public class Member{
 private Long id;
 private String name;
 private String email;
 private String password;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;
}
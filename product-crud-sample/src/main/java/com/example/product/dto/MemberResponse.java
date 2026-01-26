package com.example.product.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Builder
public class MemberResponse{
 private Long id;
 private String name;
 private String email;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;
}
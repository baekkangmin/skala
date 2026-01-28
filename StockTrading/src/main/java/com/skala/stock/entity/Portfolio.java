package com.skala.stock.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자의 주식 보유 현황을 저장하는 엔티티
 * 사용자가 보유한 주식의 수량과 평균 매수가를 관리하며,
 * 사용자와 주식 간의 다대다 관계를 나타냅니다.
 */
@Entity
@Table(name = "portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // ManyToOn: 사용자 한 명은 여러 포트폴리오를 가질 수 있습니다. fetch = FetchType.LAZY: 지연 로딩 설정
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // ManyToOne: 주식 한 종목은 여러 포트폴리오에 포함될 수 있습니다.
    @JoinColumn(name = "stock_id", nullable = false) // @JoinColumn은 항상 상대 엔티티의 PK를 참조
    private Stock stock;

    @Column(nullable = false)
    private Long quantity; // 보유 수량

    @Column(nullable = false)
    private Long averagePrice; // 평균 매수가

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

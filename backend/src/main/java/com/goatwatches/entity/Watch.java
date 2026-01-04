package com.goatwatches.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "watches")
public class Watch {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String brand;
    
    @Column(name = "reference_number", unique = true)
    private String referenceNumber;
    
    @Column(nullable = false)
    private String model;
    
    @Column(name = "watch_year")
    private Integer year;
    
    @Column(length = 2000)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "net_votes")
    private Integer netVotes = 0;

    @Column(name = "upvotes")
    private Integer upvotes = 0;

    @Column(name = "downvotes")
    private Integer downvotes = 0;

    @Column(name = "ranking_score")
    private Double rankingScore = 0.0;

    @Column(name = "system_rank")
    private Integer systemRank = 0;
    
    @Column(name = "price")
    private String price;

    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

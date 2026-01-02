package com.goatwatches.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "votes")
public class Vote {
    @Id
    private String id;
    
    @Column(name = "watch_id", nullable = false)
    private String watchId;
    
    @Column(name = "voter_token", nullable = false)
    private String voterToken;
    
    @Column(name = "vote_value", nullable = false)
    private Integer voteValue;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
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

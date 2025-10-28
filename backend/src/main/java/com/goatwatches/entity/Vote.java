package com.goatwatches.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getWatchId() { return watchId; }
    public void setWatchId(String watchId) { this.watchId = watchId; }
    
    public String getVoterToken() { return voterToken; }
    public void setVoterToken(String voterToken) { this.voterToken = voterToken; }
    
    public Integer getVoteValue() { return voteValue; }
    public void setVoteValue(Integer voteValue) { this.voteValue = voteValue; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

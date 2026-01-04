package com.goatwatches.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

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
    
    @Column(name = "price")
    private String price;
    
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
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    
    public Integer getNetVotes() { return netVotes; }
    public void setNetVotes(Integer netVotes) { this.netVotes = netVotes; }
    
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
}

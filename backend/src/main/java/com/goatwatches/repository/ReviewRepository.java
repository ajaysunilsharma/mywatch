package com.goatwatches.repository;

import com.goatwatches.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    Page<Review> findByWatchIdOrderByCreatedAtDesc(String watchId, Pageable pageable);
    long countByWatchId(String watchId);
}

package com.goatwatches.repository;

import com.goatwatches.entity.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchRepository extends JpaRepository<Watch, String> {
    boolean existsByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);
    Page<Watch> findAllByOrderByNetVotesDesc(Pageable pageable);
    Page<Watch> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT w FROM Watch w LEFT JOIN Review r ON r.watchId = w.id " +
           "GROUP BY w.id ORDER BY COUNT(r.id) DESC")
    Page<Watch> findAllOrderByReviewCountDesc(Pageable pageable);
}

package com.goatwatches.repository;

import com.goatwatches.entity.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WatchRepository extends JpaRepository<Watch, String> {
    boolean existsByReferenceNumber(String referenceNumber);
    boolean existsByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);
    
    Page<Watch> findByNetVotesGreaterThanOrderByNetVotesDesc(int minVotes, Pageable pageable);

    @Query("SELECT w FROM Watch w ORDER BY CASE WHEN w.systemRank > 0 THEN 0 ELSE 1 END, w.systemRank ASC, w.rankingScore DESC")
    Page<Watch> findTopRatedWatches(Pageable pageable);
    
    Page<Watch> findByReviewCountGreaterThanOrderByReviewCountDesc(int minReviews, Pageable pageable);
    
    Page<Watch> findAllByOrderByReviewCountDescNetVotesDesc(Pageable pageable);

    @Query("SELECT w FROM Watch w WHERE LOWER(w.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(w.model) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Watch> searchWatches(@Param("query") String query);
}
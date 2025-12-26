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
    Page<Watch> findAllByOrderByNetVotesDesc(Pageable pageable);
    Page<Watch> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Watch> findAllByOrderByYearDescCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT w FROM Watch w LEFT JOIN Review r ON r.watchId = w.id " +
           "GROUP BY w.id ORDER BY COUNT(r.id) DESC")
    Page<Watch> findAllOrderByReviewCountDesc(Pageable pageable);

    @Query("SELECT w FROM Watch w WHERE LOWER(w.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(w.model) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Watch> searchWatches(@Param("query") String query);
}
package com.goatwatches.service;

import com.goatwatches.entity.Review;
import com.goatwatches.entity.Vote;
import com.goatwatches.entity.Watch;
import com.goatwatches.repository.ReviewRepository;
import com.goatwatches.repository.VoteRepository;
import com.goatwatches.repository.WatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WatchService {
    
    @Autowired
    private WatchRepository watchRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public Page<Watch> getWatches(String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        return switch (sort != null ? sort : "top") {
            case "new" -> watchRepository.findAllByOrderByCreatedAtDesc(pageable);
            case "reviews" -> watchRepository.findAllOrderByReviewCountDesc(pageable);
            default -> watchRepository.findAllByOrderByNetVotesDesc(pageable);
        };
    }
    
    public Optional<Watch> getWatch(String id) {
        return watchRepository.findById(id);
    }
    
    public Watch createWatch(Watch watch) {
        if (watchRepository.existsByBrandIgnoreCaseAndModelIgnoreCase(
            watch.getBrand(), watch.getModel())) {
            throw new IllegalArgumentException("Watch already exists");
        }
        return watchRepository.save(watch);
    }
    
    @Transactional
    public void deleteWatch(String id) {
        Watch watch = watchRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Watch not found"));
        
        if (watch.getThumbnailUrl() != null) {
            fileStorageService.deleteFile(watch.getThumbnailUrl());
        }
        
        Page<Review> reviews = reviewRepository.findByWatchIdOrderByCreatedAtDesc(
            id, PageRequest.of(0, 1000));
        for (Review review : reviews) {
            if (review.getImageUrl() != null) {
                fileStorageService.deleteFile(review.getImageUrl());
            }
        }
        
        reviewRepository.deleteAll(reviews);
        voteRepository.deleteAll(voteRepository.findAll().stream()
            .filter(v -> v.getWatchId().equals(id))
            .toList());
        watchRepository.delete(watch);
    }
    
    public Page<Review> getReviews(String watchId, int page, int size) {
        return reviewRepository.findByWatchIdOrderByCreatedAtDesc(
            watchId, PageRequest.of(page, size));
    }
    
    public Review createReview(Review review) {
        watchRepository.findById(review.getWatchId())
            .orElseThrow(() -> new IllegalArgumentException("Watch not found"));
        return reviewRepository.save(review);
    }
    
    @Transactional
    public void deleteReview(String id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        if (review.getImageUrl() != null) {
            fileStorageService.deleteFile(review.getImageUrl());
        }
        
        reviewRepository.delete(review);
    }
    
    public long getReviewCount(String watchId) {
        return reviewRepository.countByWatchId(watchId);
    }
    
    @Transactional
    public Watch vote(String watchId, String voterToken, int voteValue) {
        Watch watch = watchRepository.findById(watchId)
            .orElseThrow(() -> new IllegalArgumentException("Watch not found"));
        
        Optional<Vote> existingVote = voteRepository.findByWatchIdAndVoterToken(watchId, voterToken);
        
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            int oldValue = vote.getVoteValue();
            
            if (oldValue == voteValue) {
                return watch;
            }
            
            vote.setVoteValue(voteValue);
            voteRepository.save(vote);
            
            int netChange = voteValue - oldValue;
            watch.setNetVotes(watch.getNetVotes() + netChange);
        } else {
            Vote vote = new Vote();
            vote.setWatchId(watchId);
            vote.setVoterToken(voterToken);
            vote.setVoteValue(voteValue);
            voteRepository.save(vote);
            
            watch.setNetVotes(watch.getNetVotes() + voteValue);
        }
        
        return watchRepository.save(watch);
    }
}

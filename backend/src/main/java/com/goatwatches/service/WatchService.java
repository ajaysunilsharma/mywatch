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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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

    @Autowired
    private AuthService authService;

    public Page<Watch> getWatches(String sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        return switch (sort != null ? sort : "top") {
            case "new" -> watchRepository.findAllByOrderByYearDescCreatedAtDesc(pageable);
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

    public Watch createWatch(String brand, String model, Integer year, String description, String createdBy, MultipartFile image) throws IOException {
        Watch watch = new Watch();
        watch.setBrand(brand);
        watch.setModel(model);
        watch.setYear(year);
        watch.setDescription(description);
        watch.setCreatedBy(createdBy != null ? createdBy : "Anonymous");

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            watch.setThumbnailUrl(imageUrl);
        }

        return createWatch(watch);
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

    public Review createReview(String id, String content, MultipartFile image) throws IOException {
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Review must be 1000 characters or less");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var response = authService.getCurrentUser(authentication);

        Review review = new Review();
        review.setWatchId(id);
        review.setAuthorName(response.getUsername());
        review.setContent(content);

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            review.setImageUrl(imageUrl);
        }

        return createReview(review);
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
        Vote vote = null;
        
        if (existingVote.isPresent()) {
            vote = existingVote.get();
            int oldValue = vote.getVoteValue();

            // upvoting after an upvote or downvoting after a downvote
            if (oldValue == voteValue) {
                return watch;
            }

            int finalVoteCountForUser = oldValue + voteValue;
            vote.setVoteValue(finalVoteCountForUser);
            if(finalVoteCountForUser == 0){
                // 0 vote entries are not saved
                voteRepository.deleteById(vote.getId());
            }else{
                voteRepository.save(vote);
            }
        } else {
            vote = new Vote();
            vote.setWatchId(watchId);
            vote.setVoterToken(voterToken);
            vote.setVoteValue(voteValue);
            voteRepository.save(vote);
        }
        watch.setNetVotes(watch.getNetVotes() + voteValue);
        return watchRepository.save(watch);
    }

    public Optional<Watch> updateWatch(String id, String brand, String model, String referenceNumber, Integer year, String price, String description, MultipartFile image) {
        return watchRepository.findById(id).map(watch -> {
            watch.setBrand(brand);
            watch.setModel(model);
            watch.setReferenceNumber(referenceNumber);
            watch.setYear(year);
            watch.setPrice(price);
            watch.setDescription(description);

            if (image != null && !image.isEmpty()) {
                // In a real app, delete the old image if necessary
                String imageUrl = null;
                try {
                    imageUrl = fileStorageService.storeFile(image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                watch.setThumbnailUrl(imageUrl);
            }

            return watchRepository.save(watch);
        });
    }

    public List<Watch> searchWatches(String query) {
        return watchRepository.searchWatches(query);
    }
}

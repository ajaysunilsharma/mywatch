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
            case "all" -> watchRepository.findAllByOrderByReviewCountDescNetVotesDesc(pageable);
            case "reviews" -> watchRepository.findByReviewCountGreaterThanOrderByReviewCountDesc(0, pageable);
            default -> watchRepository.findTopRatedWatches(pageable);
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
    
    @Transactional
    public Review createReview(Review review) {
        Watch watch = watchRepository.findById(review.getWatchId())
            .orElseThrow(() -> new IllegalArgumentException("Watch not found"));

        Review savedReview = reviewRepository.save(review);

        // Update review count
        if (watch.getReviewCount() == null) {
            watch.setReviewCount(0);
        }
        watch.setReviewCount(watch.getReviewCount() + 1);
        watchRepository.save(watch);

        return savedReview;
    }

    @Transactional
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
        
        // Update review count
        watchRepository.findById(review.getWatchId()).ifPresent(watch -> {
            if (watch.getReviewCount() == null) {
                watch.setReviewCount(0);
            }
            watch.setReviewCount(Math.max(0, watch.getReviewCount() - 1));
            watchRepository.save(watch);
        });

        reviewRepository.delete(review);
    }
    
    public long getReviewCount(String watchId) {
        return reviewRepository.countByWatchId(watchId);
    }
    
    @Transactional
    public Watch vote(String watchId, String voterToken, int voteValue) {
        Watch watch = watchRepository.findById(watchId)
            .orElseThrow(() -> new IllegalArgumentException("Watch not found"));
        
        // Initialize null vote counts
        if (watch.getUpvotes() == null) watch.setUpvotes(0);
        if (watch.getDownvotes() == null) watch.setDownvotes(0);

        Optional<Vote> existingVote = voteRepository.findByWatchIdAndVoterToken(watchId, voterToken);
        Vote vote = null;

        if (existingVote.isPresent()) {
            vote = existingVote.get();
            int oldValue = vote.getVoteValue();

            // upvoting after an upvote or downvoting after a downvote
            if (oldValue == voteValue) {
                return watch;
            }
            
            vote.setVoteValue(voteValue);
            voteRepository.save(vote);
            
            // Update upvotes/downvotes
            if (oldValue == 1) {
                watch.setUpvotes(Math.max(0, watch.getUpvotes() - 1));
            } else if (oldValue == -1) {
                watch.setDownvotes(Math.max(0, watch.getDownvotes() - 1));
            }

            if (voteValue == 1) {
                watch.setUpvotes(watch.getUpvotes() + 1);
            } else if (voteValue == -1) {
                watch.setDownvotes(watch.getDownvotes() + 1);
            }

            int netChange = voteValue - oldValue;
            watch.setNetVotes(watch.getNetVotes() + netChange);
        } else {
            vote = new Vote();
            vote.setWatchId(watchId);
            vote.setVoterToken(voterToken);
            vote.setVoteValue(voteValue);
            voteRepository.save(vote);
            
            if (voteValue == 1) {
                watch.setUpvotes(watch.getUpvotes() + 1);
            } else if (voteValue == -1) {
                watch.setDownvotes(watch.getDownvotes() + 1);
            }

            watch.setNetVotes(watch.getNetVotes() + voteValue);
        }

        // Calculate Wilson Score
        watch.setRankingScore(calculateWilsonScore(watch.getUpvotes(), watch.getDownvotes()));
        
        return watchRepository.save(watch);
    }

    /**
     * Calculates the <a href="https://en.wikipedia.org/wiki/Binomial_proportion_confidence_interval#Wilson_score_interval">Wilson Score Interval</a> for ranking.
     *
     * The Wilson Score Interval estimates the "true" popularity of an item given a small sample size.
     * It balances the proportion of positive votes with the uncertainty of having few votes.
     *
     * @param upvotes   Number of positive votes
     * @param downvotes Number of negative votes
     * @return The lower bound of the Wilson score confidence interval
     */
    private double calculateWilsonScore(int upvotes, int downvotes) {
        int n = upvotes + downvotes;
        if (n == 0) return 0.0;

        // z is the z-score for the desired confidence level.
        // 1.96 corresponds to a 95% confidence level in a normal distribution.
        // This means we are 95% confident that the true score falls within the calculated interval.
        double z = 1.96;

        // phat (p-hat) is the observed proportion of positive votes.
        double phat = (double) upvotes / n;

        // The formula for the lower bound of the Wilson score interval:
        // (phat + z^2/(2n) - z * sqrt((phat*(1-phat) + z^2/(4n))/n)) / (1 + z^2/n)
        return (phat + z*z/(2*n) - z * Math.sqrt((phat*(1-phat) + z*z/(4*n))/n)) / (1 + z*z/n);
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

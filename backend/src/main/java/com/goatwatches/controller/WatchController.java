package com.goatwatches.controller;

import com.goatwatches.entity.Review;
import com.goatwatches.entity.Watch;
import com.goatwatches.repository.WatchRepository;
import com.goatwatches.service.FileStorageService;
import com.goatwatches.service.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/watches")
@CrossOrigin(origins = "*")
public class WatchController {

    @Autowired
    private WatchService watchService;

    @Autowired
    private WatchRepository watchRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWatches(
            @RequestParam(defaultValue = "top") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Watch> watchPage = watchService.getWatches(sort, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("watches", watchPage.getContent());
        response.put("currentPage", watchPage.getNumber());
        response.put("totalPages", watchPage.getTotalPages());
        response.put("totalItems", watchPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWatch(@PathVariable String id) {
        return watchService.getWatch(id)
                .map(watch -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("watch", watch);
                    response.put("reviewCount", watchService.getReviewCount(id));
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createWatch(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam(required = false) Integer year,
            @RequestParam String description,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) MultipartFile image) {

        try {
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

            Watch savedWatch = watchService.createWatch(watch);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create watch"));
        }
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> getReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Review> reviewPage = watchService.getReviews(id, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewPage.getContent());
        response.put("currentPage", reviewPage.getNumber());
        response.put("totalPages", reviewPage.getTotalPages());
        response.put("totalItems", reviewPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable String id,
            @RequestParam(required = false) String authorName,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image) {

        try {
            if (content.length() > 1000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Review must be 1000 characters or less"));
            }

            Review review = new Review();
            review.setWatchId(id);
            review.setAuthorName(authorName != null ? authorName : "Anonymous");
            review.setContent(content);

            if (image != null && !image.isEmpty()) {
                String imageUrl = fileStorageService.storeFile(image);
                review.setImageUrl(imageUrl);
            }

            Review savedReview = watchService.createReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create review"));
        }
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<?> vote(
            @PathVariable String id,
            @RequestBody Map<String, Object> voteRequest) {

        try {
            int vote = (Integer) voteRequest.get("vote");
            String voterToken = (String) voteRequest.get("voterToken");

            if (vote != 1 && vote != -1) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vote must be 1 or -1"));
            }

            if (voterToken == null || voterToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Voter token required"));
            }

            Watch updatedWatch = watchService.vote(id, voterToken, vote);
            return ResponseEntity.ok(updatedWatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to vote"));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Watch> updateWatch(
            @PathVariable Long id,
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("referenceNumber") String referenceNumber,
            @RequestParam("year") Integer year,
            @RequestParam("price") String price,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return watchRepository.findById(id.toString()).map(watch -> {
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

            return ResponseEntity.ok(watchRepository.save(watch));
        }).orElse(ResponseEntity.notFound().build());
    }

}

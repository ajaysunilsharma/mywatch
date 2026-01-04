package com.goatwatches.controller;

import com.goatwatches.entity.Review;
import com.goatwatches.entity.Watch;
import com.goatwatches.service.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/watches")
public class WatchController {

    @Autowired
    private WatchService watchService;

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

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Watch>> searchWatches(@RequestParam String query) {
        if (query == null || query.length() < 3) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(watchService.searchWatches(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWatch(@PathVariable String id) {
        return watchService.getWatch(id)
                .map(watch -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("watch", watch);
                    response.put("reviewCount", watchService.getReviewCount(id));
                    return ResponseEntity.ok()
                            .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                            .body(response);
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
            Watch savedWatch = watchService.createWatch(brand, model, year, description, createdBy, image);
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
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image) {

        try {
            Review savedReview = watchService.createReview(id, content, image);
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
            @RequestBody Map<String, Object> voteRequest,
            Principal principal) {

        try {
            int vote = (Integer) voteRequest.get("vote");

            if (vote != 1 && vote != -1) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vote must be 1 or -1"));
            }

            Watch updatedWatch = watchService.vote(id, principal.getName(), vote);
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
            @PathVariable String id,
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("referenceNumber") String referenceNumber,
            @RequestParam("year") Integer year,
            @RequestParam("price") String price,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return watchService.updateWatch(id, brand, model, referenceNumber, year, price, description, image)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWatch(@PathVariable String id) {
        try {
            watchService.deleteWatch(id);
            return ResponseEntity.ok(Map.of("message", "Watch deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

}

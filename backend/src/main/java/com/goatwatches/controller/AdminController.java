package com.goatwatches.controller;

import com.goatwatches.service.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private WatchService watchService;
    
    @Value("${admin.token}")
    private String adminToken;
    
    @DeleteMapping("/watches/{id}")
    public ResponseEntity<?> deleteWatch(
            @PathVariable String id,
            @RequestParam String token) {
        
        if (!adminToken.equals(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            watchService.deleteWatch(id);
            return ResponseEntity.ok(Map.of("message", "Watch deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String id,
            @RequestParam String token) {
        
        if (!adminToken.equals(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            watchService.deleteReview(id);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

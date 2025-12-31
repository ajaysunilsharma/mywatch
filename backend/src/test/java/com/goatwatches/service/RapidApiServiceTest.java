package com.goatwatches.service;

import com.goatwatches.dto.rapid.SearchWatchPayload;
import com.goatwatches.dto.rapid.SearchWatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RapidApiServiceTest {

    @Autowired
    private RapidApiService rapidApiService;

    @Test
    public void testGetAllWatchMakes() {
        Map<Integer, String> makes = rapidApiService.getAllWatchMakes();
        assertNotNull(makes);
        assertFalse(makes.isEmpty());
        // Check for a known brand, e.g., Rolex
        assertTrue(makes.containsValue("Rolex"));
    }

    @Test
    public void testSearchWatch() {
        SearchWatchPayload payload = new SearchWatchPayload("Casio", 1, 10);

        SearchWatchResponse response = rapidApiService.searchWatch(payload);
        assertNotNull(response);
        assertFalse(response.getWatches().isEmpty());
        assertNotNull(response.getWatches().getFirst());
        // Add more assertions based on expected response structure
        // For example, check if results are not empty if the search is expected to return data
    }
}

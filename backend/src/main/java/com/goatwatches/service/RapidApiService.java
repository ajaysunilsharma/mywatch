package com.goatwatches.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.goatwatches.dto.rapid.SearchWatchPayload;
import com.goatwatches.dto.rapid.SearchWatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RapidApiService {

    private final RestClient restClient;

    public RapidApiService(RestClient.Builder builder,
                           @Value("${api.rapid.host}") String hostName,
                           @Value("${api.rapid.key}") String apiKey) {

        final String baseUrl = "https://" + hostName;

        log.info("baseUrl -> {}, hostName -> {}", baseUrl, hostName);

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-host", hostName)
                .defaultHeader("x-rapidapi-key", apiKey)
                .build();
    }

    public Map<Integer, String> getAllWatchMakes() {
        log.info("Fetching all watch makes");
        final var jsonResult = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/make")
                        .build())
                .retrieve()
                .body(JsonNode.class);

        Map<Integer, String> makeMap = new HashMap<>();
        if (jsonResult != null && jsonResult.has("make")) {
            for (JsonNode node : jsonResult.get("make")) {
                makeMap.put(node.get("makeId").asInt(), node.get("makeName").asText());
            }
        }
        log.info("Fetched {} watch makes", makeMap.size());
        return makeMap;
    }

    public SearchWatchResponse searchWatch(SearchWatchPayload searchWatchPayload) {
        log.info("Searching watch with payload: {}", searchWatchPayload);
        
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("searchTerm", searchWatchPayload.searchTerm());
        formData.add("page", String.valueOf(searchWatchPayload.page()));
        formData.add("limit", String.valueOf(searchWatchPayload.limit()));

        return restClient.post()
                .uri("/search-watches-by-name")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(SearchWatchResponse.class);
    }
}

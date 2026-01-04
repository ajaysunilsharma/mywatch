package com.goatwatches.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.goatwatches.dto.rapid.RapidApiWatch;
import com.goatwatches.dto.rapid.SearchWatchPayload;
import com.goatwatches.dto.rapid.SearchWatchResponse;
import lombok.NonNull;
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

    private enum SearchType {

        NAME("/search-watches-by-name"), REFERENCE("/search-reference");

        private final String url;

        SearchType(String url) {
            this.url = url;
        }

    }

    private static final String IMAGE_API_BASE_URL = "http://api-watches-v2.makingdatameaningful.com";

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

    public SearchWatchResponse getWatchesByMakeId(int makeId, int page, int limit){
        log.info("Fetching watches for makeId: {} page: {} limit: {}", makeId, page, limit);
        return restClient.get().uri(uriBuilder -> uriBuilder
                .path("/watches")
                .path("/make/").path(String.valueOf(makeId))
                .path("/page/").path(String.valueOf(page))
                .path("/limit/").path(String.valueOf(limit))
                .build())
                .retrieve()
                .body(SearchWatchResponse.class);
    }

    public SearchWatchResponse searchWatchByName(SearchWatchPayload searchWatchPayload) {
        return searchWatch(searchWatchPayload, SearchType.NAME);
    }

    public SearchWatchResponse searchWatchByReference(SearchWatchPayload searchWatchPayload) {
        return searchWatch(searchWatchPayload, SearchType.REFERENCE);
    }

    private SearchWatchResponse searchWatch(SearchWatchPayload searchWatchPayload, SearchType searchType){
        log.info("Searching watch with payload: {}", searchWatchPayload);

        MultiValueMap<String, String> formData = getFormData(searchWatchPayload, searchType);

        return restClient.post()
                .uri(searchType.url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(SearchWatchResponse.class);
    }

    private static MultiValueMap<String, String> getFormData(SearchWatchPayload searchWatchPayload,
                                                             SearchType searchType) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        if (SearchType.NAME.equals(searchType)) {
            formData.add("searchTerm", searchWatchPayload.toSearch());
        } else if (SearchType.REFERENCE.equals(searchType)){
            formData.add("referenceSearchTerm", searchWatchPayload.toSearch());
        }

        formData.add("page", String.valueOf(searchWatchPayload.page()));
        formData.add("limit", String.valueOf(searchWatchPayload.limit()));
        return formData;
    }

    public static String getImgUrl(@NonNull RapidApiWatch watch) {
        return String.format("%s/files/watches/%d/watch/%s", 
            IMAGE_API_BASE_URL, 
            watch.getWatchId(), 
            watch.getWatchImageName());
    }
}

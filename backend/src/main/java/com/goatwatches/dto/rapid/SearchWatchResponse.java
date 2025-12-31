package com.goatwatches.dto.rapid;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchWatchResponse {

    private int count;
    private int page;
    private int allPages;
    private int limit;
    private List<RapidApiWatch> watches;

}

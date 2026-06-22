package com.carmatch.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuggestionResponse {
    private Integer rank;
    private CarResponse car;
    private Double score;
    private Integer scorePercentage;
    private String reasoning;
}
package com.carmatch.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RecommendationResponse {
    private CarSummaryResponse car;
    private Double score;
    private Integer scorePercentage;
    private List<String> matchedCriteria;
}
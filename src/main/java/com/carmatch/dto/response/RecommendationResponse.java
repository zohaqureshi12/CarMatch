package com.carmatch.dto.response;

import java.util.List;

public class RecommendationResponse {

    private CarSummaryResponse car;
    private Double score;
    private Integer scorePercentage;
    private List<String> matchedCriteria;

    public CarSummaryResponse getCar() { return car; }
    public void setCar(CarSummaryResponse car) { this.car = car; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Integer scorePercentage) { this.scorePercentage = scorePercentage; }

    public List<String> getMatchedCriteria() { return matchedCriteria; }
    public void setMatchedCriteria(List<String> matchedCriteria) { this.matchedCriteria = matchedCriteria; }
}
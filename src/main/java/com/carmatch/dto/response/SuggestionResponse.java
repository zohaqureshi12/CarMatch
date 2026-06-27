package com.carmatch.dto.response;

public class SuggestionResponse {

    private Integer rank;
    private CarResponse car;
    private Double score;
    private Integer scorePercentage;
    private String reasoning;

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public CarResponse getCar() { return car; }
    public void setCar(CarResponse car) { this.car = car; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Integer scorePercentage) { this.scorePercentage = scorePercentage; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
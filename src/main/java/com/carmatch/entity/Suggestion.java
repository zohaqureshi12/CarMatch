package com.carmatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "suggestions")
public class Suggestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Integer rankPosition;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    public Suggestion() {}

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Integer getRankPosition() { return rankPosition; }
    public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
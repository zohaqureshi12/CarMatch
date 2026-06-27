package com.carmatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_responses")
public class UserResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(nullable = false)
    private String questionKey;

    @Column(nullable = false)
    private String answerValue;

    public UserResponse() {}

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public String getQuestionKey() { return questionKey; }
    public void setQuestionKey(String questionKey) { this.questionKey = questionKey; }

    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }
}
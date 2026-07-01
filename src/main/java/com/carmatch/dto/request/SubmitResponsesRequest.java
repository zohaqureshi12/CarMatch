package com.carmatch.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmitResponsesRequest {

    @Valid
    @NotEmpty(message = "Responses cannot be empty")
    private List<QuestionAnswer> responses;

    public List<QuestionAnswer> getResponses() { return responses; }
    public void setResponses(List<QuestionAnswer> responses) { this.responses = responses; }

    public static class QuestionAnswer {

        @NotBlank(message = "Question key is required")
        private String questionKey;

        @NotBlank(message = "Answer is required")
        private String answerValue;

        public String getQuestionKey() { return questionKey; }
        public void setQuestionKey(String questionKey) { this.questionKey = questionKey; }

        public String getAnswerValue() { return answerValue; }
        public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }
    }
}
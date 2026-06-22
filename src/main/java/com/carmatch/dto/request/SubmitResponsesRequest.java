package com.carmatch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SubmitResponsesRequest {

    @NotNull(message = "Responses are required")
    private List<QuestionAnswer> responses;

    @Getter
    @Setter
    public static class QuestionAnswer {

        @NotBlank(message = "Question key is required")
        private String questionKey;

        @NotBlank(message = "Answer is required")
        private String answerValue;
    }
}
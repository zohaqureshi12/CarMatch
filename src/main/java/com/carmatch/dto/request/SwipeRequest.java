package com.carmatch.dto.request;

import com.carmatch.enums.SwipeDirection;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwipeRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Direction is required")
    private SwipeDirection direction;
}
package com.carmatch.dto.request;

import com.carmatch.enums.SwipeDirection;
import jakarta.validation.constraints.NotNull;

public class SwipeRequest {

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Direction is required")
    private SwipeDirection direction;

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public SwipeDirection getDirection() { return direction; }
    public void setDirection(SwipeDirection direction) { this.direction = direction; }
}
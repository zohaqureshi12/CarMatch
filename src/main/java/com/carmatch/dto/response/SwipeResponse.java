package com.carmatch.dto.response;

import com.carmatch.enums.SwipeDirection;
import java.time.LocalDateTime;

public class SwipeResponse {

    private Long id;
    private Long carId;
    private String carBrand;
    private String carModel;
    private SwipeDirection direction;
    private LocalDateTime swipedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public String getCarBrand() { return carBrand; }
    public void setCarBrand(String carBrand) { this.carBrand = carBrand; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public SwipeDirection getDirection() { return direction; }
    public void setDirection(SwipeDirection direction) { this.direction = direction; }

    public LocalDateTime getSwipedAt() { return swipedAt; }
    public void setSwipedAt(LocalDateTime swipedAt) { this.swipedAt = swipedAt; }
}
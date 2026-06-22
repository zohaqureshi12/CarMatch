package com.carmatch.dto.response;

import com.carmatch.enums.SwipeDirection;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SwipeResponse {
    private Long id;
    private Long carId;
    private String carBrand;
    private String carModel;
    private SwipeDirection direction;
    private LocalDateTime swipedAt;
}
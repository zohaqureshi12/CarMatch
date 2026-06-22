package com.carmatch.dto.response;

import com.carmatch.enums.SessionStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SessionResponse {
    private Long id;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer responseCount;
    private Integer swipeCount;
}
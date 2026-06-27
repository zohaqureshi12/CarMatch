package com.carmatch.dto.response;

import com.carmatch.enums.SessionStatus;
import java.time.LocalDateTime;

public class SessionResponse {

    private Long id;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer responseCount;
    private Integer swipeCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getResponseCount() { return responseCount; }
    public void setResponseCount(Integer responseCount) { this.responseCount = responseCount; }

    public Integer getSwipeCount() { return swipeCount; }
    public void setSwipeCount(Integer swipeCount) { this.swipeCount = swipeCount; }
}
package com.carmatch.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
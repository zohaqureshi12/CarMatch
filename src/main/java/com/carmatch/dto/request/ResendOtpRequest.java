package com.carmatch.dto.request;

import jakarta.validation.constraints.*;

public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
package com.goatwatches.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank(message = "Email or username is required")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
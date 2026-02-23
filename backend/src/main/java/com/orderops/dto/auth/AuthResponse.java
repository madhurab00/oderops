package com.orderops.dto.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String accessToken;
    String tokenType;
    long expiresIn;
    UserResponse user;
}
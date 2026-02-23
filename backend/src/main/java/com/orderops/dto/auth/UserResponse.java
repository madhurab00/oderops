package com.orderops.dto.auth;

import com.orderops.entity.Role;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class UserResponse {
    UUID id;
    String name;
    String email;
    Role role;
}
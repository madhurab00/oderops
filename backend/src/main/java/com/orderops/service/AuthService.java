package com.orderops.service;

import com.orderops.dto.auth.AuthResponse;
import com.orderops.dto.auth.LoginRequest;
import com.orderops.dto.auth.RegisterRequest;
import com.orderops.dto.auth.UserResponse;
import com.orderops.entity.Role;
import com.orderops.entity.User;
import com.orderops.exception.ConflictException;
import com.orderops.exception.NotFoundException;
import com.orderops.repository.UserRepository;
import com.orderops.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }

        User u = new User();
        u.setName(req.getName().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.STAFF);

        User saved = userRepository.save(u);

        return UserResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Invalid credentials"));

        boolean ok = passwordEncoder.matches(req.getPassword(), user.getPasswordHash());
        if (!ok) {
            throw new NotFoundException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getRole());

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userResponse)
                .build();
    }
}
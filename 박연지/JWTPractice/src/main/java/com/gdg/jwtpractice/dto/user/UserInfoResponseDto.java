package com.gdg.jwtpractice.dto.user;

import com.gdg.jwtpractice.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserInfoResponseDto(
        Long id,
        String username,
        String email,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserInfoResponseDto from(User user) {
        return UserInfoResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

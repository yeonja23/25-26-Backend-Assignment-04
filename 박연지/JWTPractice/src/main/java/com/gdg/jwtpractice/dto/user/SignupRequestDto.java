package com.gdg.jwtpractice.dto.user;

public record SignupRequestDto(
        String username,
        String email,
        String password
) {
}

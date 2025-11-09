package com.gdg.jwtpractice.dto.user;

public record LoginRequestDto(
        String email,
        String password
) {
}

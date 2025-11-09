package com.gdg.jwtpractice.dto.post;

public record PostSaveRequestDto(
        String title,
        String content
) {
}

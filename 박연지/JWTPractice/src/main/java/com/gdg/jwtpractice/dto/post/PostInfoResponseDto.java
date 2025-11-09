package com.gdg.jwtpractice.dto.post;

import com.gdg.jwtpractice.domain.Post;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostInfoResponseDto(
        Long id,
        String title,
        String content,
        String username,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostInfoResponseDto from(Post post) {
        return PostInfoResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .username(post.getUser().getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}

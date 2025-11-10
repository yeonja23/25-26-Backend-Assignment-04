package com.gdg.jwtpractice.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String token;
    private LocalDateTime expiration;

    public void updateToken(String newToken, LocalDateTime newExpiration) {
        this.token = newToken;
        this.expiration = newExpiration;
    }

    public boolean isExpired() {
        return expiration.isBefore(LocalDateTime.now());
    }
}

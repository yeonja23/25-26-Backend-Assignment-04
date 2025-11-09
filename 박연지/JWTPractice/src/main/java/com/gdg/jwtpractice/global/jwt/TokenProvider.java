package com.gdg.jwtpractice.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

// 토큰을 생성하고 올바른 토큰인지 유효성 검사를 하고, 토큰에서 필요한 정보를 가져오는 클래스
@Slf4j
@Component
public class TokenProvider {
    private static final String ROLE_CLAIM = "role";
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";

    private final SecretKey key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${ACCESS_TOKEN_VALIDITY_IN_MILLISECONDS}") long accessTokenValidityTime,
            @Value("${REFRESH_TOKEN_VALIDITY_IN_MILLISECONDS}") long refreshTokenValidityTime
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    // AccessToken 생성
    public String createAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        Date accessTokenExpiredTime = new Date(now.getTime() + accessTokenValidityTime);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim(ROLE_CLAIM, role)
                .claim("token_type", "access_token")
                .issuedAt(now)
                .expiration(accessTokenExpiredTime)
                .signWith(key)
                .compact();
    }

    // RefreshToken 생성
    public String createRefreshToken(Long userId, String email) {
        Date now = new Date();
        Date refreshTokenExpiredTime = new Date(now.getTime() + refreshTokenValidityTime);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("token_type", "refresh_token")
                .issuedAt(now)
                .expiration(refreshTokenExpiredTime)
                .signWith(key)
                .compact();
    }

    // Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("token_type", String.class);

        if ("refresh_token".equals(tokenType)) {
            throw new RuntimeException("리프레시 토큰으로 인증을 시도할 수 없습니다.");
        }

        String role = claims.get(ROLE_CLAIM, String.class);

        if (role == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(role.split(","))
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
        authentication.setDetails(claims);

        return authentication;
    }

    // 토큰 분해 및 해석
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(BEARER.length());
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 형식: {}", e.getMessage());
        } catch (SecurityException | IllegalArgumentException | UnsupportedJwtException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
        }
        return false;
    }

    // Claims 파싱
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (SecurityException e) {
            throw new RuntimeException("토큰 복호화에 실패했습니다.");
        }
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }
}

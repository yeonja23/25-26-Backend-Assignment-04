package com.gdg.jwtpractice.service;

import com.gdg.jwtpractice.domain.User;
import com.gdg.jwtpractice.domain.RefreshToken;
import com.gdg.jwtpractice.dto.user.LoginRequestDto;
import com.gdg.jwtpractice.dto.user.TokenDto;
import com.gdg.jwtpractice.global.code.ErrorStatus;
import com.gdg.jwtpractice.global.exception.GeneralException;
import com.gdg.jwtpractice.global.jwt.TokenProvider;
import com.gdg.jwtpractice.repository.UserRepository;
import com.gdg.jwtpractice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // 로그인 및 토큰 발급
    public TokenDto login(LoginRequestDto requestDto) {
        User user = authenticateUser(requestDto);
        TokenDto tokens = generateTokens(user);
        String savedRefreshToken = saveOrUpdateRefreshToken(user.getId(), tokens.refreshToken());
        return new TokenDto(tokens.accessToken(), savedRefreshToken);
    }

    // 토큰 재발급
    public TokenDto reissueAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        Long userId = tokenProvider.getUserId(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_MISMATCH);
        }
        if (savedToken.isExpired()) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        String newAccessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return new TokenDto(newAccessToken, refreshToken);
    }

    // 헬퍼 메소드들
    private User authenticateUser(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new GeneralException(ErrorStatus.INVALID_PASSWORD);
        }
        return user;
    }

    private TokenDto generateTokens(User user) {
        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.createRefreshToken(user.getId(), user.getEmail());
        return new TokenDto(accessToken, refreshToken);
    }

    private String saveOrUpdateRefreshToken(Long userId, String refreshToken) {
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .map(token -> {
                    token.updateToken(refreshToken, LocalDateTime.now().plusDays(7));
                    return token;
                })
                .orElseGet(() -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .userId(userId)
                                .token(refreshToken)
                                .expiration(LocalDateTime.now().plusDays(7))
                                .build()
                ));
        return savedToken.getToken();
    }

    private void validateRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_INVALID);
        }
    }
}

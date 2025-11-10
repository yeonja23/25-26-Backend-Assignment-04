package com.gdg.jwtpractice.service;

import com.gdg.jwtpractice.domain.Role;
import com.gdg.jwtpractice.domain.User;
import com.gdg.jwtpractice.domain.RefreshToken;
import com.gdg.jwtpractice.dto.user.LoginRequestDto;
import com.gdg.jwtpractice.dto.user.SignupRequestDto;
import com.gdg.jwtpractice.dto.user.TokenDto;
import com.gdg.jwtpractice.dto.user.UserInfoResponseDto;
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

    // 회원가입
    public UserInfoResponseDto signup(SignupRequestDto requestDto) {
        duplicateEmailValidation(requestDto.email());

        String encodedPassword = passwordEncoder.encode(requestDto.password());

        User user = User.builder()
                .username(requestDto.username())
                .email(requestDto.email())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        return UserInfoResponseDto.from(userRepository.save(user));
    }

    private void duplicateEmailValidation(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.USER_ALREADY_EXISTS);
        }
    }

    // 로그인 + 토큰 발급
    public TokenDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new GeneralException(ErrorStatus.INVALID_PASSWORD);
        }

        String accessToken = tokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.createRefreshToken(user.getId(), user.getEmail());

        // 리프레시 토큰 저장 또는 갱신
        RefreshToken savedToken = refreshTokenRepository.findByUserId(user.getId())
                .map(token -> {
                    token.updateToken(refreshToken, LocalDateTime.now().plusDays(7));
                    return token;
                })
                .orElseGet(() -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .userId(user.getId())
                                .token(refreshToken)
                                .expiration(LocalDateTime.now().plusDays(7))
                                .build()
                ));

        return new TokenDto(accessToken, savedToken.getToken());
    }

    public TokenDto reissueAccessToken(String refreshToken) {
        // RefreshToken 유효성 검사
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_INVALID);
        }

        Long userId = tokenProvider.getUserId(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_MISMATCH);
        }

        if (savedToken.isExpired()) {
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        // AccessToken 새로 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        String newAccessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 새 액세스토큰 + 기존 리프레시토큰 반환
        return new TokenDto(newAccessToken, refreshToken);
    }

}

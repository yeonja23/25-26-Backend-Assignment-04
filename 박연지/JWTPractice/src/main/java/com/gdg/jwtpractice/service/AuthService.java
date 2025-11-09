package com.gdg.jwtpractice.service;

import com.gdg.jwtpractice.domain.Role;
import com.gdg.jwtpractice.domain.User;
import com.gdg.jwtpractice.domain.RefreshToken;
import com.gdg.jwtpractice.dto.user.LoginRequestDto;
import com.gdg.jwtpractice.dto.user.SignupRequestDto;
import com.gdg.jwtpractice.dto.user.TokenDto;
import com.gdg.jwtpractice.dto.user.UserInfoResponseDto;
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
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.password());

        User user = User.builder()
                .username(requestDto.username())
                .email(requestDto.email())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        return UserInfoResponseDto.from(userRepository.save(user));
    }

    // 로그인 + 토큰 발급
    public TokenDto login(LoginRequestDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = tokenProvider.getUserId(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        if (savedToken.isExpired()) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다. 다시 로그인하세요.");
        }

        // AccessToken 새로 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String newAccessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 새 액세스토큰 + 기존 리프레시토큰 반환
        return new TokenDto(newAccessToken, refreshToken);
    }

}

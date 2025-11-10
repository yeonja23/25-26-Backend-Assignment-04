package com.gdg.jwtpractice.service;

import com.gdg.jwtpractice.domain.Role;
import com.gdg.jwtpractice.domain.User;
import com.gdg.jwtpractice.dto.user.SignupRequestDto;
import com.gdg.jwtpractice.dto.user.UserInfoResponseDto;
import com.gdg.jwtpractice.global.code.ErrorStatus;
import com.gdg.jwtpractice.global.exception.GeneralException;
import com.gdg.jwtpractice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserInfoResponseDto signup(SignupRequestDto requestDto) {
        validateDuplicateEmail(requestDto.email());

        User user = User.builder()
                .username(requestDto.username())
                .email(requestDto.email())
                .password(passwordEncoder.encode(requestDto.password()))
                .role(Role.USER)
                .build();

        return UserInfoResponseDto.from(userRepository.save(user));
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException(ErrorStatus.USER_ALREADY_EXISTS);
        }
    }
}


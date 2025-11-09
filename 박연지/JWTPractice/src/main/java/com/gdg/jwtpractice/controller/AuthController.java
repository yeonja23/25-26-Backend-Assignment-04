package com.gdg.jwtpractice.controller;

import com.gdg.jwtpractice.dto.user.LoginRequestDto;
import com.gdg.jwtpractice.dto.user.RefreshTokenDto;
import com.gdg.jwtpractice.dto.user.SignupRequestDto;
import com.gdg.jwtpractice.dto.user.TokenDto;
import com.gdg.jwtpractice.dto.user.UserInfoResponseDto;
import com.gdg.jwtpractice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserInfoResponseDto> signup(@RequestBody SignupRequestDto requestDto) {
        return ResponseEntity.ok(authService.signup(requestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody RefreshTokenDto requestDto) {
        return ResponseEntity.ok(authService.reissueAccessToken(requestDto.refreshToken()));
    }
}

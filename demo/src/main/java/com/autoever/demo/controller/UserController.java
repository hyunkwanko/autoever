package com.autoever.demo.controller;

import com.autoever.demo.dto.*;
import com.autoever.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Validated @RequestBody UserSignupRequest userSignupRequest)
    {
        userService.signup(userSignupRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인 (JWT 발급)
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(
            @Validated @RequestBody UserLoginRequest userLoginRequest)
    {
        String token = userService.login(userLoginRequest);
        return ResponseEntity.ok(new UserLoginResponse(token));
    }

    /**
     * 내 정보 조회
     * - JWT 인증 필요
     * - 주소는 시/도 단위만 반환
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String username = authentication.getName();
        UserResponse me = userService.getMe(username);
        return ResponseEntity.ok(me);
    }
}

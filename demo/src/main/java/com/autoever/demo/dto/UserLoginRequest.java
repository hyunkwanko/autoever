package com.autoever.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLoginRequest {

    private String username;    // 계정
    private String password;    // 비밀번호

    @Builder
    public UserLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

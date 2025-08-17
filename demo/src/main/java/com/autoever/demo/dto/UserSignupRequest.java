package com.autoever.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignupRequest {

    private String username;    // 계정
    private String password;    // 비밀번호
    private String name;        // 성명
    private String ssn;         // 주민등록번호
    private String phone;       // 휴대폰 번호
    private String address;     // 주소

    @Builder
    public UserSignupRequest(String username, String password, String name, String ssn, String phone, String address) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.ssn = ssn;
        this.phone = phone;
        this.address = address;
    }
}

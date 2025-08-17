package com.autoever.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponse {

    private Long id;            // ID
    private String username;    // 계정
    private String name;        // 성명
    private String ssn;         // 주민등록번호
    private String phone;       // 휴대폰 번호
    private String address;     // 주소 "서울특별시" 처럼 시/도 단위만

    @Builder
    public UserResponse(Long id, String username, String name, String ssn, String phone, String address) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.ssn = ssn;
        this.phone = phone;
        this.address = address;
    }
}

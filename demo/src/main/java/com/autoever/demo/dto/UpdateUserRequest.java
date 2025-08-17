package com.autoever.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUserRequest {

    private String password; // 변경할 비밀번호
    private String address;  // 변경할 주소

    @Builder
    public UpdateUserRequest(String password, String address) {
        this.password = password;
        this.address = address;
    }
}

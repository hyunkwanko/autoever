package com.autoever.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageTaskRequest {

    private String phone;   // 휴대폰 번호
    private String name;    // 성명
    private String message; // 메시지 내용

    @Builder
    public MessageTaskRequest(String phone, String name, String message) {
        this.phone = phone;
        this.name = name;
        this.message = message;
    }
}

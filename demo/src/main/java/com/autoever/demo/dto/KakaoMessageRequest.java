package com.autoever.demo.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoMessageRequest {

    private String phone;   // 휴대폰 번호
    private String message; // 메시지 내용

    @Builder
    public KakaoMessageRequest(String phone, String message) {
        this.phone = phone;
        this.message = message;
    }
}

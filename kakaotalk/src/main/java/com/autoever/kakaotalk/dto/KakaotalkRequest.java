package com.autoever.kakaotalk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaotalkRequest {

    private String phone;    // 휴대폰 번호
    private String message;  // 메시지 내용
}
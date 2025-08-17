package com.autoever.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private String ageGroup; // 예: "20", "30" (연령대 필터)
    private String message;  // 보낼 메시지 본문 (첫줄 제외)
}

package com.autoever.kakaotalk.controller;

import com.autoever.kakaotalk.dto.KakaotalkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@Slf4j
@RestController
public class KakaotalkController {

    @Value("${kakao.auth.username}")
    private String kakaoUsername;

    @Value("${kakao.auth.password}")
    private String kakaoPassword;

    @PostMapping("/kakaotalk-messages")
    public ResponseEntity<Void> sendKakao(
            @RequestHeader("Authorization") String auth,
            @RequestBody KakaotalkRequest kakaotalkRequest
    ) {
        try {
            if (!isAuthorized(auth)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 실제 발송 로직 (테스트용: 항상 성공)
            log.info("[카카오] 발송 성공: {} / 메시지: {}", kakaotalkRequest.getPhone(), kakaotalkRequest.getMessage());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("[카카오] 발송 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isAuthorized(String auth) {
        if (auth == null || !auth.startsWith("Basic ")) return false;

        String base64Credentials = auth.substring("Basic ".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);

        return values.length == 2 &&
                kakaoUsername.equals(values[0]) &&
                kakaoPassword.equals(values[1]);
    }
}
package com.autoever.sms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
public class SmsController {

    @Value("${sms.auth.username}")
    private String smsUsername;

    @Value("${sms.auth.password}")
    private String smsPassword;

    @PostMapping("/sms")
    public ResponseEntity<Map<String, String>> sendSms(
            @RequestHeader("Authorization") String auth,
            @RequestParam String phone,
            @RequestParam String message // ★ RequestBody 대신 RequestParam
    ) {
        try {
            if (!isAuthorized(auth)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("result", "FAIL", "reason", "Unauthorized"));
            }

            log.info("[SMS] 발송 성공: {} / 메시지: {}", phone, message);
            return ResponseEntity.ok(Map.of("result", "OK"));

        } catch (Exception e) {
            log.error("[SMS] 발송 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("result", "FAIL", "reason", e.getMessage()));
        }
    }

    private boolean isAuthorized(String auth) {
        if (auth == null || !auth.startsWith("Basic ")) return false;

        String base64Credentials = auth.substring("Basic ".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] values = credentials.split(":", 2);

        return values.length == 2 &&
                smsUsername.equals(values[0]) &&
                smsPassword.equals(values[1]);
    }
}


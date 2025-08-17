package com.autoever.demo.service;

import com.autoever.demo.dto.KakaoMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class MessageService {

    private final WebClient kakaoClient;
    private final WebClient smsClient;

    public MessageService(WebClient kakaoClient, WebClient smsClient) {
        this.kakaoClient = kakaoClient;
        this.smsClient = smsClient;
    }

    public boolean sendKakaoMessage(String phone, String message) {
        try {
            var res = kakaoClient.post()
                    .uri("/kakaotalk-messages")
                    .bodyValue(
                            KakaoMessageRequest.builder()
                                    .phone(phone)
                                    .message(message)
                                    .build()
                    )
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            boolean success = res != null && res.getStatusCode().is2xxSuccessful();
            log.info("카카오톡 발송 결과 → phone={}, message='{}', status={}, success={}",
                    phone, message, (res != null ? res.getStatusCode() : "null"), success);

            return success;
        } catch (Exception e) {
            log.error("카카오톡 발송 실패 → phone={}, message='{}', error={}",
                    phone, message, e.getMessage(), e);
            return false;
        }
    }

    public boolean sendSms(String phone, String message) {
        try {
            String res = smsClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/sms")
                            .queryParam("phone", phone)
                            .build())
                    .body(BodyInserters.fromFormData("message", message))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            boolean success = res != null && res.contains("OK");

            log.info("[SMS 요청] phone={}, message='{}'", phone, message);
            log.info("[SMS 응답] response='{}', success={}", res, success);

            return success;
        } catch (Exception e) {
            log.error("[SMS 발송 실패] phone={}, message='{}', error={}",
                    phone, message, e.getMessage(), e);
            return false;
        }
    }
}

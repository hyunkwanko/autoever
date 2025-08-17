package com.autoever.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${external.auth.username}")
    private String username;

    @Value("${external.auth.kakao}")
    private String kakaoPassword;

    @Value("${external.auth.sms}")
    private String smsPassword;

    @Bean
    public WebClient kakaoClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081")
                .defaultHeaders(h -> h.setBasicAuth(username, kakaoPassword))
                .build();
    }

    @Bean
    public WebClient smsClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8082")
                .defaultHeaders(h -> h.setBasicAuth(username, smsPassword))
                .build();
    }
}

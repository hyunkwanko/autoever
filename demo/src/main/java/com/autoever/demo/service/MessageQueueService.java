package com.autoever.demo.service;

import com.autoever.demo.dto.MessageTaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {

    private final MessageService messageService;

    private final BlockingQueue<MessageTaskRequest> kakaoQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<MessageTaskRequest> smsQueue = new LinkedBlockingQueue<>();

    public void enqueueKakao(MessageTaskRequest messageTaskRequest) {
        kakaoQueue.offer(messageTaskRequest);
    }

    public void enqueueSms(MessageTaskRequest messageTaskRequest) {
        smsQueue.offer(messageTaskRequest);
    }

    // 카카오톡: 분당 100건
    @Scheduled(fixedRate = 60_000)
    @Async
    public void processKakaoMessages() {
        int limit = 100;
        log.info("[카카오] 전송 작업 시작 - 최대 {}건 / 현재 큐 크기: {}", limit, kakaoQueue.size());

        int processedCount = 0;
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < limit; i++) {
            MessageTaskRequest task = kakaoQueue.poll();
            if (task == null) {
                log.info("[카카오] 큐에 더 이상 작업이 없습니다. (총 {}건 처리)", processedCount);
                break;
            }

            processedCount++;
            log.debug("[카카오] 처리 대상: {} / 이름: {} / 내용: {}", task.getPhone(), task.getName(), task.getMessage());

            boolean sent = messageService.sendKakaoMessage(
                    task.getPhone(),
                    task.getName() + "님, 안녕하세요. 현대 오토에버입니다.\n" + task.getMessage()
            );

            if (sent) {
                successCount++;
                log.info("[카카오] 발송 성공: {}", task.getPhone());
            } else {
                failCount++;
                log.warn("[카카오] 발송 실패 → SMS로 대체: {}", task.getPhone());
                enqueueSms(task);
            }
        }

        log.info("[카카오] 전송 작업 종료 - 총 처리: {}건 / 성공: {}건 / 실패: {}건 / 잔여 큐: {}",
                processedCount, successCount, failCount, kakaoQueue.size());
    }

    // SMS: 분당 500건
    @Scheduled(fixedRate = 60_000)
    @Async
    public void processSmsMessages() {
        int limit = 500;
        log.info("[SMS] 전송 작업 시작 - 최대 {}건 / 현재 큐 크기: {}", limit, smsQueue.size());

        int processedCount = 0;
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < limit; i++) {
            MessageTaskRequest task = smsQueue.poll();
            if (task == null) {
                log.info("[SMS] 큐에 더 이상 작업이 없습니다. (총 {}건 처리)", processedCount);
                break;
            }

            processedCount++;
            log.debug("[SMS] 처리 대상: {} / 이름: {} / 내용: {}", task.getPhone(), task.getName(), task.getMessage());

            boolean sent = messageService.sendSms(
                    task.getPhone(),
                    task.getName() + "님, 안녕하세요. 현대 오토에버입니다.\n" + task.getMessage()
            );

            if (sent) {
                successCount++;
                log.info("[SMS] 발송 성공: {}", task.getPhone());
            } else {
                failCount++;
                log.error("[SMS] 발송 실패: {}", task.getPhone());
            }
        }

        log.info("[SMS] 전송 작업 종료 - 총 처리: {}건 / 성공: {}건 / 실패: {}건 / 잔여 큐: {}",
                processedCount, successCount, failCount, smsQueue.size());
    }
}

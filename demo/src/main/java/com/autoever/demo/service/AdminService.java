package com.autoever.demo.service;

import com.autoever.demo.dto.MessageTaskRequest;
import com.autoever.demo.dto.UpdateUserRequest;
import com.autoever.demo.dto.UserResponse;
import com.autoever.demo.entity.User;
import com.autoever.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final MessageQueueService messageQueueService;

    /**
     * 회원 목록 조회
     */
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user ->
                UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .ssn(user.getSsn())
                        .phone(user.getPhone())
                        .address(user.getAddress())
                        .build()
        );
    }

    /**
     * 회원 수정 (비밀번호, 주소만)
     */
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (updateUserRequest.getPassword() != null && !updateUserRequest.getPassword().isBlank()) {
            user.setPassword(encoder.encode(updateUserRequest.getPassword()));
        }

        if (updateUserRequest.getAddress() != null && !updateUserRequest.getAddress().isBlank()) {
            user.setAddress(updateUserRequest.getAddress());
        }

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .ssn(user.getSsn())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    /**
     * 회원 삭제
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        userRepository.deleteById(id);
    }

    /**
     * 연령대별 메시지 발송
     */
    public Map<String, Object> sendMessagesByAgeGroup(String ageGroup, String message) {
        log.info("연령대별 메시지 발송 요청: ageGroup={}, message={}", ageGroup, message);

        List<User> targets = filterByAgeGroup(ageGroup);

        log.info("필터링된 대상 수: {}", targets.size());

        int total = targets.size();

        for (User user : targets) {
            log.debug("큐에 등록: {} / {}", user.getName(), user.getPhone());
            messageQueueService.enqueueKakao(
                    MessageTaskRequest.builder()
                            .phone(user.getPhone())
                            .name(user.getName())
                            .message(message)
                            .build()
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalQueued", total);
        result.put("status", "발송 요청이 큐에 등록되었습니다. 실제 발송은 분당 제한에 따라 순차 처리됩니다.");

        return result;
    }

    private List<User> filterByAgeGroup(String ageGroup) {
        int startAge, endAge;
        switch (ageGroup) {
            case "20" -> { startAge = 20; endAge = 29; }
            case "30" -> { startAge = 30; endAge = 39; }
            case "40" -> { startAge = 40; endAge = 49; }
            default -> { startAge = 0; endAge = 200; }
        }

        int currentYear = LocalDate.now().getYear();

        return userRepository.findAll().stream()
                .filter(user -> {
                    try {
                        if (user.getSsn() == null || user.getSsn().length() < 7) {
                            log.warn("잘못된 주민번호: {} (사용자: {})", user.getSsn(), user.getName());
                            return false;
                        }

                        String birthStr = user.getSsn().substring(0, 6);
                        int birthYear = Integer.parseInt(birthStr.substring(0, 2));

                        char genderCode = user.getSsn().charAt(7);
                        int century;
                        if (genderCode == '1' || genderCode == '2') century = 1900;
                        else if (genderCode == '3' || genderCode == '4') century = 2000;
                        else {
                            log.warn("주민번호 7자리 잘못됨: {} (사용자: {})", user.getSsn(), user.getName());
                            return false;
                        }

                        birthYear += century;
                        int age = currentYear - birthYear;
                        boolean inGroup = age >= startAge && age <= endAge;

                        log.debug("사용자: {}, 나이: {}, 연령대: {}대, 포함여부: {}", user.getName(), age, ageGroup, inGroup);
                        return inGroup;
                    } catch (Exception e) {
                        log.error("주민번호 파싱 실패: {} (사용자: {}), error: {}", user.getSsn(), user.getName(), e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}

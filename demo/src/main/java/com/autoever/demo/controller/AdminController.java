package com.autoever.demo.controller;

import com.autoever.demo.dto.SendMessageRequest;
import com.autoever.demo.dto.UpdateUserRequest;
import com.autoever.demo.dto.UserResponse;
import com.autoever.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 회원 목록 조회 (페이징)
     * 예: GET /api/admin/users?page=0&size=20&sort=id,desc
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        Page<UserResponse> page = adminService.listUsers(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * 회원 수정 (비밀번호, 주소만)
     * - 둘 중 하나만 또는 둘 다 수정 가능
     */
    @PatchMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Validated @RequestBody UpdateUserRequest updateUserRequest
    ) {
        UserResponse updated = adminService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(updated);
    }

    /**
     * 회원 삭제
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 연령대별 카카오톡 발송 (실패 시 SMS 대체)
     * - 카카오: 분당 100건 제한
     * - SMS: 분당 500건 제한
     * - 첫 줄은 "{회원 성명}님, 안녕하세요. 현대 오토에버입니다."
     * - 요청 예: { "ageGroup": "30", "content": "8월 프로모션 안내드립니다." }
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Validated @RequestBody SendMessageRequest sendMessageRequest)
    {
        // 서비스에서 연령대 필터링 + 카카오 우선 발송 + 실패시 SMS, 분당 제한 처리
        var result = adminService.sendMessagesByAgeGroup(sendMessageRequest.getAgeGroup(), sendMessageRequest.getMessage());
        return ResponseEntity.ok(result);
    }
}

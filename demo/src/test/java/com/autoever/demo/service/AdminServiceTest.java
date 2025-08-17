package com.autoever.demo.service;

import com.autoever.demo.dto.MessageTaskRequest;
import com.autoever.demo.dto.UpdateUserRequest;
import com.autoever.demo.dto.UserResponse;
import com.autoever.demo.entity.User;
import com.autoever.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private MessageQueueService messageQueueService;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 회원목록조회() {
        // given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .name("홍길동")
                .ssn("9001011234567")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .build();

        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        Page<UserResponse> result = adminService.listUsers(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void 회원수정_비밀번호와주소() {
        // given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("old")
                .address("서울시 강남구")
                .build();

        UpdateUserRequest updateUserRequest = UpdateUserRequest.builder()
                .password("newpass")
                .address("부산시 해운대구")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encoder.encode("newpass")).thenReturn("encoded");

        // when
        UserResponse response = adminService.updateUser(1L, updateUserRequest);

        // then
        assertThat(response.getAddress()).isEqualTo("부산시 해운대구");
        verify(userRepository).save(user);
    }

    @Test
    void 회원삭제() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);

        // when
        adminService.deleteUser(1L);

        // then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void 회원삭제_존재하지않음() {
        // given
        when(userRepository.existsById(1L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    void 연령대별메시지발송() {
        // given
        User user20 = User.builder()
                .id(1L).name("철수").phone("010-1111-2222")
                .username("user1")
                .ssn("000101-3123456") // ✅ 2000년생, 남자 (genderCode=3)
                .build();

        User user30 = User.builder()
                .id(2L).name("영희").phone("010-3333-4444")
                .username("user2")
                .ssn("900101-2123456") // ✅ 1990년생, 여자 (genderCode=2)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user20, user30));

        // when
        Map<String, Object> result = adminService.sendMessagesByAgeGroup("20", "안녕하세요");

        // then
        assertThat(result.get("totalQueued")).isEqualTo(1);
        verify(messageQueueService, times(1)).enqueueKakao(any(MessageTaskRequest.class));
    }
}

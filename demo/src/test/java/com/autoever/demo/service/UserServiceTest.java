package com.autoever.demo.service;

import com.autoever.demo.config.JwtTokenProvider;
import com.autoever.demo.dto.UserLoginRequest;
import com.autoever.demo.dto.UserResponse;
import com.autoever.demo.dto.UserSignupRequest;
import com.autoever.demo.entity.User;
import com.autoever.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 회원가입_성공() {
        // given
        UserSignupRequest userSignupRequest = UserSignupRequest.builder()
                .username("testuser")
                .password("1234")
                .name("홍길동")
                .ssn("1234567890123")
                .phone("010-1111-2222")
                .address("서울특별시 강남구")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsBySsn("1234567890123")).thenReturn(false);
        when(encoder.encode("1234")).thenReturn("encoded");

        // when
        userService.signup(userSignupRequest);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 회원가입_아이디중복_예외() {
        // given
        UserSignupRequest userSignupRequest = UserSignupRequest.builder()
                .username("dupUser")
                .password("pw")
                .ssn("1234567890123")
                .build();

        when(userRepository.existsByUsername("dupUser")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(userSignupRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 존재하는 계정");
    }

    @Test
    void 로그인_성공() {
        // given
        User user = User.builder()
                .id(1L)
                .username("tester")
                .password("encodedPw")
                .build();

        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username("tester")
                .password("plainPw")
                .build();

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(encoder.matches("plainPw", "encodedPw")).thenReturn(true);
        when(jwtTokenProvider.createToken("tester")).thenReturn("jwt-token");

        // when
        String token = userService.login(userLoginRequest);

        // then
        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void 로그인_비밀번호불일치_예외() {
        // given
        User user = User.builder()
                .username("tester")
                .password("encodedPw")
                .build();

        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .username("tester")
                .password("wrongPw")
                .build();

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(encoder.matches("wrongPw", "encodedPw")).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(userLoginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    @Test
    void 내정보조회_주소는시도단위만() {
        // given
        User user = User.builder()
                .id(1L)
                .username("tester")
                .name("홍길동")
                .ssn("1234567890123")
                .phone("010-9999-8888")
                .address("서울특별시 강남구 테헤란로")
                .build();

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        // when
        UserResponse response = userService.getMe("tester");

        // then
        assertThat(response.getAddress()).isEqualTo("서울특별시");
    }
}

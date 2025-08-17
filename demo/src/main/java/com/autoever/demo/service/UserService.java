package com.autoever.demo.service;

import com.autoever.demo.config.JwtTokenProvider;
import com.autoever.demo.dto.UserLoginRequest;
import com.autoever.demo.dto.UserResponse;
import com.autoever.demo.dto.UserSignupRequest;
import com.autoever.demo.entity.User;
import com.autoever.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    public void signup(UserSignupRequest userSignupRequest) {
        // 계정 중복 체크
        if (userRepository.existsByUsername(userSignupRequest.getUsername()))
            throw new RuntimeException("이미 존재하는 계정입니다.");

        // 주민등록번호 길이 체크
        if (userSignupRequest.getSsn() == null || userSignupRequest.getSsn().length() != 13)
            throw new RuntimeException("주민등록번호는 13자리여야 합니다.");

        // 주민등록번호 중복 체크
        if (userRepository.existsBySsn(userSignupRequest.getSsn()))
            throw new RuntimeException("이미 존재하는 주민등록번호입니다.");

        User user = User.builder()
                .username(userSignupRequest.getUsername())
                .password(encoder.encode(userSignupRequest.getPassword()))
                .name(userSignupRequest.getName())
                .ssn(userSignupRequest.getSsn())
                .phone(userSignupRequest.getPhone())
                .address(userSignupRequest.getAddress())
                .build();

        userRepository.save(user);
    }

    /**
     * 로그인 (JWT 발급)
     */
    public String login(UserLoginRequest userLoginRequest) {
        User user = userRepository.findByUsername(userLoginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("계정을 찾을 수 없습니다."));

        if (!encoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(user.getUsername());
    }

    /**
     * 내 정보 조회 (주소는 시/도 단위만)
     */
    public UserResponse getMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .ssn(user.getSsn())
                .phone(user.getPhone())
                .address(extractTopLevelAddress(user.getAddress()))
                .build();
    }

    private String extractTopLevelAddress(String address) {
        if (address == null || address.isBlank()) return "";
        return address.split(" ")[0];
    }
}

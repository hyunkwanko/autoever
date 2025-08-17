package com.autoever.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"ssn"})
})
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;    // 계정
    private String password;    // 비밀번호
    private String name;        // 성명
    private String ssn;         // 주민등록번호
    private String phone;       // 휴대폰 번호
    private String address;     // 주소
}

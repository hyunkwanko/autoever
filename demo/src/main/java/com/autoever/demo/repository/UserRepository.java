package com.autoever.demo.repository;

import com.autoever.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);
    boolean existsBySsn(String ssn);
    Optional<User> findByUsername(String username);
}

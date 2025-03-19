package com.pcclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pcclub.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(String phoneNumber);
}
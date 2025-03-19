package com.pcclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pcclub.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByEmail(String email);
}
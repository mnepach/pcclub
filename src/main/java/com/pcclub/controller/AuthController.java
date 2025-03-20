package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.pcclub.dto.AuthRequest;
import com.pcclub.dto.JwtResponse;
import com.pcclub.dto.UserRequest;
import com.pcclub.model.Admin;
import com.pcclub.model.User;
import com.pcclub.repository.AdminRepository;
import com.pcclub.repository.UserRepository;
import com.pcclub.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest) {
        if (userRepository.findByPhoneNumber(userRequest.getPhoneNumber()) != null) {
            return ResponseEntity.badRequest().body("Phone number already in use");
        }

        User user = new User();
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setName(userRequest.getName());
        user.setBookedSeats(userRequest.getBookedSeats());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole("client");

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getPhoneNumber(), savedUser.getRole(), savedUser.getId());

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        // Проверка входа пользователя
        if (authRequest.getPhoneNumber() != null) {
            User user = userRepository.findByPhoneNumber(authRequest.getPhoneNumber());
            if (user != null && passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole(), user.getId());
                return ResponseEntity.ok(new JwtResponse(token));
            }
        }

        // Проверка входа администратора
        if (authRequest.getEmail() != null && authRequest.getUniqueCode() != null) {
            Admin admin = adminRepository.findByEmail(authRequest.getEmail());
            if (admin != null && admin.getUniqueCode().equals(authRequest.getUniqueCode())) {
                String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole(), admin.getId());
                return ResponseEntity.ok(new JwtResponse(token));
            }
        }

        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pcclub.model.User;
import com.pcclub.model.Admin;
import com.pcclub.security.JwtUtil;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.AdminRepository;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        if (authRequest.getPhoneNumber() != null) {
            User user = userRepository.findByPhoneNumber(authRequest.getPhoneNumber());
            if (user != null && passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
                String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole(), user.getId());
                return ResponseEntity.ok(new JwtResponse(token));
            }
        } else if (authRequest.getEmail() != null) {
            Admin admin = adminRepository.findByEmail(authRequest.getEmail());
            if (admin != null && authRequest.getUniqueCode().equals(admin.getUniqueCode())) {
                String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole(), admin.getId());
                return ResponseEntity.ok(new JwtResponse(token));
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest userRequest) {
        // Проверяем, существует ли пользователь с таким номером телефона
        if (userRepository.findByPhoneNumber(userRequest.getPhoneNumber()) != null) {
            return ResponseEntity.badRequest().body("User with this phone number already exists");
        }

        User user = new User();
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setName(userRequest.getName());
        user.setBookedSeats(userRequest.getBookedSeats());
        user.setRole("client");
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}

// DTO классы остаются без изменений
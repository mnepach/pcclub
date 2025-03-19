package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pcclub.model.User;
import com.pcclub.model.Admin;
import com.pcclub.security.JwtUtil;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.AdminRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        if (authRequest.getPhoneNumber() != null) {
            User user = userRepository.findByPhoneNumber(authRequest.getPhoneNumber());
            if (user != null && user.getPasswordHash().equals(authRequest.getPassword())) { // Здесь упрощенная проверка, замените на хеширование
                String token = jwtUtil.generateToken(user.getPhoneNumber());
                return ResponseEntity.ok(new JwtResponse(token));
            }
        } else if (authRequest.getEmail() != null) {
            Admin admin = adminRepository.findByEmail(authRequest.getEmail());
            if (admin != null && admin.getUniqueCode().equals(authRequest.getUniqueCode())) {
                String token = jwtUtil.generateToken(admin.getEmail());
                return ResponseEntity.ok(new JwtResponse(token));
            }
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest userRequest) {
        User user = new User();
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setName(userRequest.getName());
        user.setBookedSeats(userRequest.getBookedSeats());
        user.setRole("client");
        user.setPasswordHash(userRequest.getPassword()); // Здесь нужно хешировать пароль
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}

// DTO для запросов
class AuthRequest {
    private String phoneNumber;
    private String password;
    private String email;
    private String uniqueCode;

    // геттеры и сеттеры
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUniqueCode() { return uniqueCode; }
    public void setUniqueCode(String uniqueCode) { this.uniqueCode = uniqueCode; }
}

class UserRequest {
    private String phoneNumber;
    private String name;
    private int bookedSeats;
    private String password;

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(int bookedSeats) { this.bookedSeats = bookedSeats; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class JwtResponse {
    private String token;

    public JwtResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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
        logger.info("Попытка регистрации пользователя с номером: {}", userRequest.getPhoneNumber());

        if (userRepository.findByPhoneNumber(userRequest.getPhoneNumber()) != null) {
            logger.warn("Номер телефона уже используется: {}", userRequest.getPhoneNumber());
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

        logger.info("Пользователь успешно зарегистрирован: {}", savedUser.getId());
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        logger.info("Попытка входа. phoneNumber: {}, email: {}",
                authRequest.getPhoneNumber() != null ? authRequest.getPhoneNumber() : "не указан",
                authRequest.getEmail() != null ? authRequest.getEmail() : "не указан");

        // Проверка входа пользователя
        if (authRequest.getPhoneNumber() != null) {
            User user = userRepository.findByPhoneNumber(authRequest.getPhoneNumber());
            if (user != null) {
                logger.info("Найден пользователь с ID: {}", user.getId());

                if (passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
                    String token = jwtUtil.generateToken(user.getPhoneNumber(), user.getRole(), user.getId());
                    logger.info("Успешный вход пользователя: {}", user.getId());
                    return ResponseEntity.ok(new JwtResponse(token));
                } else {
                    logger.warn("Неверный пароль для пользователя: {}", user.getId());
                }
            } else {
                logger.warn("Пользователь не найден: {}", authRequest.getPhoneNumber());
            }
        }

        // Проверка входа администратора
        if (authRequest.getEmail() != null && authRequest.getUniqueCode() != null) {
            Admin admin = adminRepository.findByEmail(authRequest.getEmail());
            if (admin != null) {
                logger.info("Найден админ с ID: {}", admin.getId());

                if (admin.getUniqueCode().equals(authRequest.getUniqueCode())) {
                    String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole(), admin.getId());
                    logger.info("Успешный вход администратора: {}", admin.getId());
                    return ResponseEntity.ok(new JwtResponse(token));
                } else {
                    logger.warn("Неверный уникальный код для администратора: {}", admin.getId());
                }
            } else {
                logger.warn("Администратор не найден: {}", authRequest.getEmail());
            }
        }

        logger.error("Неверные учетные данные");
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    // Добавим тестовый эндпоинт, чтобы проверить, работают ли публичные методы
    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        logger.info("Тестовый публичный эндпоинт вызван");
        return ResponseEntity.ok("Публичный эндпоинт работает!");
    }
}
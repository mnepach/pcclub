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
        try {
            logger.info("Попытка регистрации пользователя с email: {}", userRequest.getEmail());

            if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
                logger.warn("Email не указан");
                return ResponseEntity.badRequest().body("Email is required");
            }

            if (userRequest.getName() == null || userRequest.getName().isEmpty()) {
                logger.warn("Имя не указано");
                return ResponseEntity.badRequest().body("Name is required");
            }

            if (userRequest.getPassword() == null || userRequest.getPassword().isEmpty()) {
                logger.warn("Пароль не указан");
                return ResponseEntity.badRequest().body("Password is required");
            }

            if (userRepository.findByemail(userRequest.getEmail()) != null) {
                logger.warn("Email уже используется: {}", userRequest.getEmail());
                return ResponseEntity.badRequest().body("Email already in use");
            }

            User user = new User();
            user.setEmail(userRequest.getEmail());
            user.setName(userRequest.getName());
            user.setBookedSeats(userRequest.getBookedSeats());
            user.setPasswordHash(passwordEncoder.encode(userRequest.getPassword()));
            user.setRole("client");

            logger.info("Сохранение пользователя в базу данных: {}", user.getEmail());
            User savedUser = userRepository.save(user);
            logger.info("Пользователь сохранен с ID: {}", savedUser.getId());

            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole(), savedUser.getId());
            logger.info("Токен сгенерирован для пользователя: {}", savedUser.getId());

            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            logger.error("Ошибка при регистрации пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            logger.info("Попытка входа. email: {}", authRequest.getEmail() != null ? authRequest.getEmail() : "не указан");

            // Проверка входа пользователя
            if (authRequest.getEmail() != null && authRequest.getPassword() != null) {
                User user = userRepository.findByemail(authRequest.getEmail());
                if (user != null) {
                    logger.info("Найден пользователь с ID: {}", user.getId());
                    if (passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
                        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                        logger.info("Успешный вход пользователя: {}", user.getId());
                        return ResponseEntity.ok(new JwtResponse(token));
                    } else {
                        logger.warn("Неверный пароль для пользователя: {}", user.getId());
                    }
                } else {
                    logger.warn("Пользователь не найден: {}", authRequest.getEmail());
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
        } catch (Exception e) {
            logger.error("Ошибка при входе: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        logger.info("Тестовый публичный эндпоинт вызван");
        return ResponseEntity.ok("Публичный эндпоинт работает!");
    }
}
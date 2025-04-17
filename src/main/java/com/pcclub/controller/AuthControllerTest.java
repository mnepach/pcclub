package com.pcclub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcclub.dto.AuthRequest;
import com.pcclub.dto.UserRequest;
import com.pcclub.model.User;
import com.pcclub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegisterUser() throws Exception {
        // Подготовка
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("newuser@example.com");
        userRequest.setName("New User");
        userRequest.setPassword("password123");
        userRequest.setBookedSeats(0);

        // Выполнение и проверка
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Дополнительная проверка в базе данных
        User savedUser = userRepository.findByemail("newuser@example.com");
        assertNotNull(savedUser);
    }

    @Test
    public void testLoginUser() throws Exception {
        // Подготовка
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole("client");
        userRepository.save(user);

        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("testuser@example.com");
        authRequest.setPassword("password123");

        // Выполнение и проверка
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        // Подготовка
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("nonexistent@example.com");
        authRequest.setPassword("wrongpassword");

        // Выполнение и проверка
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}
package com.pcclub.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcclub.dto.AuthRequest;
import com.pcclub.model.User;
import com.pcclub.repository.AdminRepository;
import com.pcclub.repository.UserRepository;
import com.pcclub.security.JwtUtil;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testPublicEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/test"))
                .andExpect(status().isOk());
    }

    @Test
    public void whenLoginWithValidCredentials_thenReturnToken() throws Exception {
        // Подготовка
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hashedPassword");
        user.setRole("client");

        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        when(userRepository.findByemail("user@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("test-token");

        // Действие и проверка
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    public void whenLoginWithInvalidCredentials_thenReturnUnauthorized() throws Exception {
        // Подготовка
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByemail("user@example.com")).thenReturn(null);

        // Действие и проверка
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
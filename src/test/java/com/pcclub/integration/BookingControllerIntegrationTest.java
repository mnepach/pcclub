package com.pcclub.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pcclub.dto.AuthRequest;
import com.pcclub.dto.BookingRequest;
import com.pcclub.model.User;
import com.pcclub.model.WorkStation;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.WorkStationRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkStationRepository workStationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    public void setup() throws Exception {
        objectMapper.registerModule(new JavaTimeModule());

        // Создаем тестового пользователя
        User user = new User();
        user.setEmail("integration@example.com");
        user.setName("Integration Test");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole("client");
        userRepository.save(user);

        // Создаем тестовую рабочую станцию
        WorkStation workStation = new WorkStation();
        workStation.setNumber("TEST-1");
        workStation.setSpecifications("Test Spec");
        workStation.setAvailable(true);
        workStation.setPricePerHour(10.0);
        workStationRepository.save(workStation);

        // Получаем токен для авторизации
        AuthRequest authRequest = new AuthRequest();
        authRequest.setEmail("integration@example.com");
        authRequest.setPassword("password");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    public void testCreateAndRetrieveBooking() throws Exception {
        // Получаем ID рабочей станции
        WorkStation workStation = workStationRepository.findByAvailable(true).get(0);

        // Создаем запрос на бронирование
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setWorkStationId(workStation.getId());
        bookingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        bookingRequest.setEndTime(LocalDateTime.now().plusHours(3));
        bookingRequest.setStatus("pending");

        // Создаем бронирование
        String bookingResponse = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long bookingId = objectMapper.readTree(bookingResponse).get("id").asLong();

        // Получаем созданное бронирование
        mockMvc.perform(get("/api/bookings/" + bookingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.totalPrice").value(20.0)); // 2 часа * 10 за час
    }
}
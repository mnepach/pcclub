package com.pcclub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcclub.model.Admin;
import com.pcclub.model.WorkStation;
import com.pcclub.repository.AdminRepository;
import com.pcclub.repository.WorkStationRepository;
import com.pcclub.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkStationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkStationRepository workStationRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Admin testAdmin;
    private String adminToken;
    private WorkStation testWorkStation;

    @BeforeEach
    public void setup() {
        // Очистка базы данных от предыдущих тестов
        workStationRepository.deleteAll();
        adminRepository.deleteAll();

        // Создаем тестового администратора
        testAdmin = new Admin();
        testAdmin.setEmail("admin@example.com");
        testAdmin.setUniqueCode("admin123");
        testAdmin.setRole("admin");
        testAdmin = adminRepository.save(testAdmin);

        // Генерируем JWT токен для администратора
        adminToken = jwtUtil.generateToken(testAdmin.getEmail(), testAdmin.getRole(), testAdmin.getId());

        // Создаем тестовую рабочую станцию
        testWorkStation = new WorkStation();
        testWorkStation.setNumber("WS-001");
        testWorkStation.setSpecifications("Intel Core i7, 16GB RAM, RTX 3080");
        testWorkStation.setAvailable(true);
        testWorkStation.setPricePerHour(100.0);
        testWorkStation = workStationRepository.save(testWorkStation);
    }

    @Test
    public void testGetAllWorkStations() throws Exception {
        // Выполнение и проверка
        mockMvc.perform(get("/api/workstations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].number").value("WS-001"));
    }

    @Test
    public void testGetWorkStationById() throws Exception {
        // Выполнение и проверка
        mockMvc.perform(get("/api/workstations/" + testWorkStation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("WS-001"))
                .andExpect(jsonPath("$.specifications").value("Intel Core i7, 16GB RAM, RTX 3080"))
                .andExpect(jsonPath("$.pricePerHour").value(100.0));
    }

    @Test
    public void testCreateWorkStation() throws Exception {
        // Подготовка
        WorkStation newWorkStation = new WorkStation();
        newWorkStation.setNumber("WS-002");
        newWorkStation.setSpecifications("AMD Ryzen 9, 32GB RAM, RTX 3090");
        newWorkStation.setAvailable(true);
        newWorkStation.setPricePerHour(150.0);

        // Выполнение и проверка
        mockMvc.perform(post("/api/workstations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWorkStation))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("WS-002"))
                .andExpect(jsonPath("$.specifications").value("AMD Ryzen 9, 32GB RAM, RTX 3090"))
                .andExpect(jsonPath("$.pricePerHour").value(150.0));
    }

    @Test
    public void testUpdateWorkStation() throws Exception {
        // Подготовка
        testWorkStation.setSpecifications("Updated: Intel Core i9, 32GB RAM, RTX 3080 Ti");
        testWorkStation.setPricePerHour(120.0);

        // Выполнение и проверка
        mockMvc.perform(put("/api/workstations/" + testWorkStation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testWorkStation))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specifications").value("Updated: Intel Core i9, 32GB RAM, RTX 3080 Ti"))
                .andExpect(jsonPath("$.pricePerHour").value(120.0));
    }

    @Test
    public void testDeleteWorkStation() throws Exception {
        // Выполнение и проверка
        mockMvc.perform(delete("/api/workstations/" + testWorkStation.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Проверка, что рабочая станция удалена
        mockMvc.perform(get("/api/workstations/" + testWorkStation.getId()))
                .andExpect(status().isNotFound());
    }
}
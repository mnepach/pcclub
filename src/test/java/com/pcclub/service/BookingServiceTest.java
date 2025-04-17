package com.pcclub.service;

import com.pcclub.dto.BookingRequest;
import com.pcclub.model.Booking;
import com.pcclub.model.User;
import com.pcclub.model.WorkStation;
import com.pcclub.repository.BookingRepository;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.WorkStationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkStationRepository workStationRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private WorkStation testWorkStation;

    @BeforeEach
    public void setup() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole("client");
        testUser = userRepository.save(testUser);

        // Создаем тестовую рабочую станцию
        testWorkStation = new WorkStation();
        testWorkStation.setNumber("WS-001");
        testWorkStation.setSpecifications("Intel Core i7, 16GB RAM, NVIDIA GeForce RTX 3080");
        testWorkStation.setAvailable(true);
        testWorkStation.setPricePerHour(100.0);
        testWorkStation = workStationRepository.save(testWorkStation);
    }

    @Test
    public void testCreateBooking() {
        // Подготовка
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setWorkStationId(testWorkStation.getId());
        bookingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        bookingRequest.setEndTime(LocalDateTime.now().plusHours(3));
        bookingRequest.setStatus("pending");

        // Выполнение
        Booking createdBooking = bookingService.createBooking(bookingRequest, testUser.getId());

        // Проверка
        assertNotNull(createdBooking);
        assertEquals(testUser.getId(), createdBooking.getUser().getId());
        assertEquals(testWorkStation.getId(), createdBooking.getWorkStation().getId());
        assertEquals("pending", createdBooking.getStatus());
        assertEquals(200.0, createdBooking.getTotalPrice()); // 2 часа * 100.0 за час
    }

    @Test
    public void testGetBookingsByUser() {
        // Подготовка
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setWorkStationId(testWorkStation.getId());
        bookingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        bookingRequest.setEndTime(LocalDateTime.now().plusHours(3));
        bookingService.createBooking(bookingRequest, testUser.getId());

        // Выполнение
        List<Booking> bookings = bookingService.getBookingsByUser(testUser.getId());

        // Проверка
        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        assertEquals(testUser.getId(), bookings.get(0).getUser().getId());
    }

    @Test
    public void testDeleteBooking() {
        // Подготовка
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setWorkStationId(testWorkStation.getId());
        bookingRequest.setStartTime(LocalDateTime.now().plusHours(1));
        bookingRequest.setEndTime(LocalDateTime.now().plusHours(3));
        Booking booking = bookingService.createBooking(bookingRequest, testUser.getId());

        // Выполнение
        boolean result = bookingService.deleteBooking(booking.getId(), testUser.getId(), "client");

        // Проверка
        assertTrue(result);
        assertTrue(bookingRepository.findById(booking.getId()).isEmpty());
    }
}
package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pcclub.model.Booking;
import com.pcclub.model.User;
import com.pcclub.model.WorkStation;
import com.pcclub.repository.BookingRepository;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.WorkStationRepository;
import com.pcclub.security.JwtUtil;
import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkStationRepository workStationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getAllBookings(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if ("admin".equals(role)) {
            return ResponseEntity.ok(bookingRepository.findAll());
        } else {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found");
            }
            return ResponseEntity.ok(bookingRepository.findByUser(user));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if ("admin".equals(role) || booking.getUser().getId().equals(userId)) {
            return ResponseEntity.ok(booking);
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        Long userId = claims.get("id", Long.class);

        User user = userRepository.findById(userId).orElse(null);
        WorkStation workStation = workStationRepository.findById(bookingRequest.getWorkStationId()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        if (workStation == null) {
            return ResponseEntity.status(404).body("WorkStation not found");
        }
        if (!workStation.isAvailable()) {
            return ResponseEntity.status(400).body("WorkStation is not available");
        }

        // Проверка на перекрытие бронирований
        List<Booking> overlappingBookings = bookingRepository.findByWorkStationAndEndTimeGreaterThanAndStartTimeLessThan(
                workStation, bookingRequest.getStartTime(), bookingRequest.getEndTime());

        if (!overlappingBookings.isEmpty()) {
            return ResponseEntity.status(400).body("This time slot is already booked");
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setWorkStation(workStation);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());

        // Расчет общей стоимости
        long hours = ChronoUnit.HOURS.between(booking.getStartTime(), booking.getEndTime());
        if (ChronoUnit.MINUTES.between(booking.getStartTime(), booking.getEndTime()) % 60 > 0) {
            hours++; // Округляем до полного часа
        }
        booking.setTotalPrice(workStation.getPricePerHour() * hours);

        booking.setStatus("confirmed");

        // Обновляем количество забронированных мест у пользователя
        user.setBookedSeats(user.getBookedSeats() + 1);
        userRepository.save(user);

        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id,
                                           @RequestBody BookingRequest bookingRequest,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if (!("admin".equals(role) || booking.getUser().getId().equals(userId))) {
            return ResponseEntity.status(403).body("Access denied");
        }

        WorkStation workStation = workStationRepository.findById(bookingRequest.getWorkStationId()).orElse(null);
        if (workStation == null) {
            return ResponseEntity.status(404).body("WorkStation not found");
        }

        // Проверка на перекрытие бронирований (если изменяется время или рабочая станция)
        if (!booking.getWorkStation().getId().equals(workStation.getId()) ||
                !booking.getStartTime().equals(bookingRequest.getStartTime()) ||
                !booking.getEndTime().equals(bookingRequest.getEndTime())) {

            List<Booking> overlappingBookings = bookingRepository.findByWorkStationAndEndTimeGreaterThanAndStartTimeLessThan(
                    workStation, bookingRequest.getStartTime(), bookingRequest.getEndTime());

            // Убираем текущее бронирование из списка перекрытий (чтобы учесть только другие бронирования)
            overlappingBookings.removeIf(b -> b.getId().equals(booking.getId()));

            if (!overlappingBookings.isEmpty()) {
                return ResponseEntity.status(400).body("This time slot is already booked");
            }
        }

        booking.setWorkStation(workStation);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());

        // Пересчитываем стоимость
        long hours = ChronoUnit.HOURS.between(booking.getStartTime(), booking.getEndTime());
        if (ChronoUnit.MINUTES.between(booking.getStartTime(), booking.getEndTime()) % 60 > 0) {
            hours++; // Округляем до полного часа
        }
        booking.setTotalPrice(workStation.getPricePerHour() * hours);

        // Статус может менять только админ
        if ("admin".equals(role) && bookingRequest.getStatus() != null) {
            booking.setStatus(bookingRequest.getStatus());
        }

        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(404).body("Booking not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if ("admin".equals(role) || booking.getUser().getId().equals(userId)) {
            // Уменьшаем количество забронированных мест у пользователя
            User user = booking.getUser();
            user.setBookedSeats(Math.max(0, user.getBookedSeats() - 1));
            userRepository.save(user);

            // Админ может удалить бронирование, клиент - только отменить
            if ("admin".equals(role)) {
                bookingRepository.delete(booking);
                return ResponseEntity.ok("Booking deleted");
            } else {
                booking.setStatus("cancelled");
                bookingRepository.save(booking);
                return ResponseEntity.ok("Booking cancelled");
            }
        }
        return ResponseEntity.status(403).body("Access denied");
    }
}

// DTO для запросов бронирования
class BookingRequest {
    private Long workStationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Long getWorkStationId() { return workStationId; }
    public void setWorkStationId(Long workStationId) { this.workStationId = workStationId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
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
import com.pcclub.dto.BookingRequest;
import com.pcclub.security.JwtUtil;
import io.jsonwebtoken.Claims;

import java.time.Duration;
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
    public ResponseEntity<?> getBooking(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
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
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        WorkStation workStation = workStationRepository.findById(bookingRequest.getWorkStationId()).orElse(null);
        if (workStation == null) {
            return ResponseEntity.status(404).body("WorkStation not found");
        }

        // Проверка доступности рабочей станции
        List<Booking> conflictingBookings = bookingRepository.findByWorkStationAndEndTimeGreaterThanAndStartTimeLessThan(
                workStation, bookingRequest.getStartTime(), bookingRequest.getEndTime());

        if (!conflictingBookings.isEmpty()) {
            return ResponseEntity.status(400).body("WorkStation is already booked for this time");
        }

        if (!workStation.isAvailable()) {
            return ResponseEntity.status(400).body("WorkStation is not available");
        }

        // Расчет стоимости
        long hours = Duration.between(bookingRequest.getStartTime(), bookingRequest.getEndTime()).toHours();
        if (hours <= 0) {
            return ResponseEntity.status(400).body("End time must be after start time");
        }

        double totalPrice = workStation.getPricePerHour() * hours;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setWorkStation(workStation);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(bookingRequest.getStatus() != null ? bookingRequest.getStatus() : "pending");

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

        if (bookingRequest.getWorkStationId() != null) {
            WorkStation workStation = workStationRepository.findById(bookingRequest.getWorkStationId()).orElse(null);
            if (workStation == null) {
                return ResponseEntity.status(404).body("WorkStation not found");
            }
            booking.setWorkStation(workStation);
        }

        if (bookingRequest.getStartTime() != null) {
            booking.setStartTime(bookingRequest.getStartTime());
        }

        if (bookingRequest.getEndTime() != null) {
            booking.setEndTime(bookingRequest.getEndTime());
        }

        if (bookingRequest.getStatus() != null) {
            booking.setStatus(bookingRequest.getStatus());
        }

        // Перерасчет стоимости
        long hours = Duration.between(booking.getStartTime(), booking.getEndTime()).toHours();
        if (hours <= 0) {
            return ResponseEntity.status(400).body("End time must be after start time");
        }

        double totalPrice = booking.getWorkStation().getPricePerHour() * hours;
        booking.setTotalPrice(totalPrice);

        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id,
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
            bookingRepository.delete(booking);
            return ResponseEntity.ok("Booking deleted");
        }
        return ResponseEntity.status(403).body("Access denied");
    }
}
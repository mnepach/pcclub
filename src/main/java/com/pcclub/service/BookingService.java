package com.pcclub.service;

import com.pcclub.dto.BookingRequest;
import com.pcclub.model.Booking;
import com.pcclub.model.User;
import com.pcclub.model.WorkStation;
import com.pcclub.repository.BookingRepository;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.WorkStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkStationRepository workStationRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(bookingRepository::findByUser).orElse(List.of());
    }

    public Optional<Booking> getBooking(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(BookingRequest bookingRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkStation workStation = workStationRepository.findById(bookingRequest.getWorkStationId())
                .orElseThrow(() -> new RuntimeException("WorkStation not found"));

        // Проверка доступности рабочей станции
        List<Booking> conflictingBookings = bookingRepository.findByWorkStationAndEndTimeGreaterThanAndStartTimeLessThan(
                workStation, bookingRequest.getStartTime(), bookingRequest.getEndTime());

        if (!conflictingBookings.isEmpty()) {
            throw new RuntimeException("WorkStation is already booked for this time");
        }

        if (!workStation.isAvailable()) {
            throw new RuntimeException("WorkStation is not available");
        }

        // Расчет стоимости
        long hours = Duration.between(bookingRequest.getStartTime(), bookingRequest.getEndTime()).toHours();
        if (hours <= 0) {
            throw new RuntimeException("End time must be after start time");
        }

        double totalPrice = workStation.getPricePerHour() * hours;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setWorkStation(workStation);
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(bookingRequest.getStatus() != null ? bookingRequest.getStatus() : "pending");

        return bookingRepository.save(booking);
    }

    public boolean deleteBooking(Long id, Long userId, String role) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return false;
        }

        Booking booking = bookingOpt.get();

        // Проверка прав на удаление
        if ("admin".equals(role) || booking.getUser().getId().equals(userId)) {
            bookingRepository.delete(booking);
            return true;
        }

        return false;
    }
}
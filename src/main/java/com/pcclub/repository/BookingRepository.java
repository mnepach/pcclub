package com.pcclub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pcclub.model.Booking;
import com.pcclub.model.User;
import com.pcclub.model.WorkStation;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByWorkStation(WorkStation workStation);
    List<Booking> findByStatus(String status);
    List<Booking> findByWorkStationAndEndTimeGreaterThanAndStartTimeLessThan(
            WorkStation workStation, LocalDateTime start, LocalDateTime end);
}
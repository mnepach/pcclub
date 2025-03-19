package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pcclub.model.WorkStation;
import com.pcclub.repository.WorkStationRepository;
import com.pcclub.security.JwtUtil;
import io.jsonwebtoken.Claims;

import java.util.List;

@RestController
@RequestMapping("/api/workstations")
public class WorkStationController {

    @Autowired
    private WorkStationRepository workStationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<WorkStation>> getAllWorkStations() {
        return ResponseEntity.ok(workStationRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkStation(@PathVariable Long id) {
        WorkStation workStation = workStationRepository.findById(id).orElse(null);
        if (workStation == null) {
            return ResponseEntity.status(404).body("WorkStation not found");
        }
        return ResponseEntity.ok(workStation);
    }

    @PostMapping
    public ResponseEntity<?> createWorkStation(@RequestBody WorkStation workStation,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            return ResponseEntity.ok(workStationRepository.save(workStation));
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkStation(@PathVariable Long id,
                                               @RequestBody WorkStation workStationDetails,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        WorkStation workStation = workStationRepository.findById(id).orElse(null);
        if (workStation == null) {
            return ResponseEntity.status(404).body("WorkStation not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            workStation.setNumber(workStationDetails.getNumber());
            workStation.setSpecifications(workStationDetails.getSpecifications());
            workStation.setAvailable(workStationDetails.isAvailable());
            workStation.setPricePerHour(workStationDetails.getPricePerHour());
            return ResponseEntity.ok(workStationRepository.save(workStation));
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkStation(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        WorkStation workStation = workStationRepository.findById(id).orElse(null);
        if (workStation == null) {
            return ResponseEntity.status(404).body("WorkStation not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            workStationRepository.delete(workStation);
            return ResponseEntity.ok("WorkStation deleted");
        }
        return ResponseEntity.status(403).body("Access denied");
    }
}
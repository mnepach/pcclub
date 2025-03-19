package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pcclub.model.User;
import com.pcclub.repository.UserRepository;
import com.pcclub.security.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7); // Удаляем "Bearer "
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            return ResponseEntity.ok(userRepository.findAll());
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if ("admin".equals(role) || userId.equals(id)) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            user.setRole("client");
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails, @RequestHeader("Authorization") String authorizationHeader) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);
        Long userId = claims.get("id", Long.class);

        if ("admin".equals(role) || userId.equals(id)) {
            user.setPhoneNumber(userDetails.getPhoneNumber());
            user.setName(userDetails.getName());
            user.setBookedSeats(userDetails.getBookedSeats());
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            userRepository.delete(user);
            return ResponseEntity.ok("User deleted");
        }
        return ResponseEntity.status(403).body("Access denied");
    }
}
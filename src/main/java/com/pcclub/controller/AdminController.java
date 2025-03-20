package com.pcclub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pcclub.model.Admin;
import com.pcclub.repository.AdminRepository;
import com.pcclub.security.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getAllAdmins(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            return ResponseEntity.ok(adminRepository.findAll());
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin, @RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            return ResponseEntity.ok(adminRepository.save(admin));
        }
        return ResponseEntity.status(403).body("Access denied");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(404).body("Admin not found");
        }

        String token = authorizationHeader.substring(7);
        Claims claims = jwtUtil.extractAllClaims(token);
        String role = claims.get("role", String.class);

        if ("admin".equals(role)) {
            adminRepository.delete(admin);
            return ResponseEntity.ok("Admin deleted");
        }
        return ResponseEntity.status(403).body("Access denied");
    }
}
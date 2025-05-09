package com.pcclub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.pcclub.model.User;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
        // Подготовка
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPasswordHash("hashedPassword");
        user.setRole("client");
        userRepository.save(user);

        // Действие
        User found = userRepository.findByemail("test@example.com");

        // Проверка
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getName()).isEqualTo("Test User");
    }

    @Test
    public void testFindByEmailNotFound() {
        // Действие
        User found = userRepository.findByemail("nonexistent@example.com");

        // Проверка
        assertThat(found).isNull();
    }
}
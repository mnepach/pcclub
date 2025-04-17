package com.pcclub;

import com.pcclub.model.User;
import com.pcclub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

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

        // Выполнение
        User foundUser = userRepository.findByemail("test@example.com");

        // Проверка
        assertNotNull(foundUser);
        assertEquals("Test User", foundUser.getName());
        assertEquals("client", foundUser.getRole());
    }

    @Test
    public void testFindByEmailNotFound() {
        // Выполнение
        User foundUser = userRepository.findByemail("nonexistent@example.com");

        // Проверка
        assertNull(foundUser);
    }
}
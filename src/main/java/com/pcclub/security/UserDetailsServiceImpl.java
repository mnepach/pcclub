package com.pcclub.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.pcclub.model.Admin;
import com.pcclub.model.User;
import com.pcclub.repository.AdminRepository;
import com.pcclub.repository.UserRepository;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Сначала ищем пользователя по номеру телефона
        User user = userRepository.findByPhoneNumber(username);

        if (user != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
            return new org.springframework.security.core.userdetails.User(
                    username,
                    user.getPasswordHash(),
                    Collections.singletonList(authority)
            );
        }

        // Если пользователь не найден, пробуем найти администратора по email
        Admin admin = adminRepository.findByEmail(username);
        if (admin != null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + admin.getRole().toUpperCase());
            return new org.springframework.security.core.userdetails.User(
                    username,
                    admin.getUniqueCode(), // используем uniqueCode как пароль для администратора
                    Collections.singletonList(authority)
            );
        }

        throw new UsernameNotFoundException("Пользователь не найден: " + username);
    }
}
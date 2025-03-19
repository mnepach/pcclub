package com.pcclub.security;

import com.pcclub.model.Admin;
import com.pcclub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.pcclub.repository.UserRepository;
import com.pcclub.repository.AdminRepository;

import static org.springframework.security.core.userdetails.User.withUsername;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(username);

        if (user != null) {
            return org.springframework.security.core.userdetails.User.withUsername(username)
                    .password(user.getPasswordHash())
                    .roles(user.getRole())
                    .build();
        }

        Admin admin = adminRepository.findByEmail(username);
        if (admin != null) {
            return org.springframework.security.core.userdetails.User.withUsername(username)
                    .password(user.getPasswordHash())
                    .roles(user.getRole())
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
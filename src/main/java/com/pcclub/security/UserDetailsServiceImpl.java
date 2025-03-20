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
        // Use unique code for admin since there's no password hash
        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password(admin.getUniqueCode()) // Fixed: was using user's password
                .roles(admin.getRole())
                .build();
    }

    throw new UsernameNotFoundException("User not found with username: " + username);
}
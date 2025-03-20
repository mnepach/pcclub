@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable() // Отключаем CSRF для REST API
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN") // Fixed: was using hasRole
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
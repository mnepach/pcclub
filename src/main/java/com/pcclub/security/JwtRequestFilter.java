package com.pcclub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Запрос к пути: {} с методом: {}", path, method);

        // Проверяем, не является ли запрос публичным
        if (path.startsWith("/api/auth/") || path.contains("/swagger-ui/") || path.contains("/v3/api-docs/") ||
                (path.startsWith("/api/workstations") && method.equals("GET"))) {
            logger.debug("Публичный путь, пропускаем без проверки токена: {}", path);
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        logger.debug("Authorization заголовок: {}", authorizationHeader);

        String username = null;
        String jwt = null;
        String role = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                role = jwtUtil.extractClaim(jwt, claims -> claims.get("role", String.class));
                logger.debug("Извлечено имя пользователя: {}, роль: {}", username, role);
            } catch (Exception e) {
                logger.error("Недействительный JWT токен: {}", e.getMessage());
            }
        } else {
            logger.warn("Отсутствует или неверный формат токена Authorization");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.debug("Загружены данные пользователя: {}", username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, Collections.singletonList(authority));
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Аутентификация успешна для пользователя: {}", username);
                } else {
                    logger.warn("Токен не прошел валидацию для пользователя: {}", username);
                }
            } catch (Exception e) {
                logger.error("Ошибка при загрузке пользователя: {}", e.getMessage());
            }
        }

        logger.debug("Продолжаем цепочку фильтров");
        chain.doFilter(request, response);
    }
}
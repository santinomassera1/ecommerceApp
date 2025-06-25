package com.example.vintagevogue.security;

import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class EmailVerificationFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    // Lista de nombres de usuario exentos de verificación
    private static final List<String> EXEMPT_USERNAMES = Arrays.asList("do", "usertest", "admin");

    // Rutas públicas que no requieren verificación
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/login", "/auth/register", "/auth/verify", "/auth/forgot-password", "/auth/reset-password",
            "/css/", "/js/", "/images/", "/vendor/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Si la ruta es pública, permitir acceso sin verificación
        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener la autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            String username = authentication.getName();
            
            // Si el usuario está en la lista de exentos, permitir acceso
            if (EXEMPT_USERNAMES.contains(username)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Verificar si el usuario ha verificado su email
            User user = userRepository.findByUsername(username);
            if (user != null && !user.isVerified()) {
                // Redirigir a una página de verificación pendiente
                response.sendRedirect("/auth/verification-required");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
} 
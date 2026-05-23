package com.cinemabooking.backend.security;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // Check if Firebase is initialized to avoid static initialization errors
            if (FirebaseApp.getApps().isEmpty()) {
                logger.error("FirebaseApp is not initialized! Check FirebaseConfig.");
                filterChain.doFilter(request, response);
                return;
            }

            // Verify token via Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            
            logger.debug("Token verified successfully: UID={}, Email={}", uid, email);

            // Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(uid, decodedToken, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // We LOG the error but do NOT block the request here.
            // This allows public endpoints to still work even if the client sends an expired token.
            // Spring Security's authorization rules will block protected endpoints later if auth is missing.
            logger.warn("Token verification failed (treating as anonymous): {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

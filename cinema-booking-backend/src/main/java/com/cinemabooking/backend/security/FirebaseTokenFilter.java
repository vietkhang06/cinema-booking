/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Security
 * 
 * Mô tả: 
 * Bộ lọc (Filter) kiểm tra tính hợp lệ của Firebase Auth Token trong header.
 * Giúp chặn các request không có quyền truy cập hoặc token đã hết hạn.
 */
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

    private static final java.util.concurrent.ConcurrentHashMap<String, String> roleCache = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Long> cacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes cache TTL

    // ZELIOUS TASK: Lấy token từ header "Authorization" của request. Chặn/Bỏ qua nếu request không có Bearer token.
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

            // ZELIOUS TASK: Xác thực token bằng Firebase Admin SDK. Đảm bảo token chưa hết hạn và do đúng Firebase phát hành.
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            
            logger.debug("Token verified successfully: UID={}, Email={}", uid, email);

            // ZELIOUS TASK: Truy vấn Role của user từ Firestore và sử dụng ConcurrentHashMap để Cache (lưu tạm) lại trong 5 phút. Việc này giúp giảm số lượng request (read) lên Firebase, tiết kiệm chi phí.
            String role = roleCache.get(uid);
            Long expiry = cacheTime.get(uid);
            long now = System.currentTimeMillis();
            if (role == null || expiry == null || expiry < now) {
                try {
                    com.google.cloud.firestore.DocumentSnapshot userDoc = 
                            com.google.firebase.cloud.FirestoreClient.getFirestore()
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .get();
                    if (userDoc.exists()) {
                        role = userDoc.getString("role");
                        if (role == null) role = "customer";
                    } else {
                        role = "customer";
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to fetch user role from Firestore for UID={}, default to 'customer': {}", uid, ex.getMessage());
                    role = "customer";
                }
                roleCache.put(uid, role);
                cacheTime.put(uid, now + CACHE_TTL_MS);
            }

            java.util.List<org.springframework.security.core.GrantedAuthority> authorities = 
                    java.util.Collections.singletonList(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                    );

            // ZELIOUS TASK: Nạp thông tin User UID và Role vào SecurityContext của Spring Security, giúp phân quyền (Admin/Customer) ở các API Controller.
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(uid, decodedToken, authorities);

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

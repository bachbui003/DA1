package com.example.ECM.service;

import com.example.ECM.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            // Lấy header Authorization
            String authHeader = request.getHeader("Authorization");

            // Kiểm tra header có hợp lệ hay không
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header");
                chain.doFilter(request, response);
                return;
            }

            // Lấy token từ header
            String token = authHeader.substring(7);

            // Kiểm tra định dạng của token
            if (token == null || token.isEmpty() || "undefined".equals(token) || token.chars().filter(ch -> ch == '.').count() != 2) {
                logger.error("Invalid JWT format: " + token);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token format");
                return;
            }

            try {
                // Trích xuất thông tin từ token
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Xác thực token
                    if (jwtUtil.validateToken(token)) {
                        String role = jwtUtil.extractRole(token);
                        String phone = jwtUtil.extractPhone(token);
                        String address = jwtUtil.extractAddress(token);
                        String email = jwtUtil.extractEmail(token);
                        String fullName = jwtUtil.extractFullName(token);

                        // Tạo Authentication token và gán vào SecurityContext
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, List.of(() -> role)
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Gán thông tin bổ sung vào request
                        request.setAttribute("role", role);
                        request.setAttribute("phone", phone);
                        request.setAttribute("address", address);
                        request.setAttribute("email", email);
                        request.setAttribute("fullName", fullName);
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing JWT: ", e);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            // Tiếp tục chuỗi filter
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Unexpected error in JWT Filter: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }
}

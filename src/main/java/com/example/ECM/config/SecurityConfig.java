package com.example.ECM.config;

import com.example.ECM.service.JwtFilter;
import com.example.ECM.service.Impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // Tắt CSRF vì chúng ta sử dụng xác thực không trạng thái (JWT)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()  // Cho phép tất cả người dùng truy cập vào các API auth
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()  // Cho phép xem sản phẩm cho tất cả người dùng
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")  // Chỉ ADMIN mới có thể thêm sản phẩm
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")  // Chỉ ADMIN mới có thể sửa sản phẩm
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")  // Chỉ ADMIN mới có thể xóa sản phẩm
                        .requestMatchers(HttpMethod.PUT, "/api/user/**").hasRole("ADMIN")  // Chỉ ADMIN mới có thể sửa thông tin người dùng
                        .requestMatchers(HttpMethod.DELETE, "/api/user/**").hasRole("ADMIN")  // Chỉ ADMIN mới có thể xóa người dùng
                        .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")  // USER & ADMIN đều có thể truy cập giỏ hàng
                        .requestMatchers("/api/cart/all").hasRole("ADMIN")  // Chỉ ADMIN mới có thể xem tất cả giỏ hàng
                        .requestMatchers("/api/v1/payments/submitOrder").hasAnyRole("USER", "ADMIN")  // USER & ADMIN có thể đặt hàng
                        .requestMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")  // USER & ADMIN có thể xem đơn hàng
                        .requestMatchers("/api/v1/payments/vnpay-payment").permitAll()  // Cho phép callback từ VNPay

                        .anyRequest().authenticated()  // Các yêu cầu khác cần phải xác thực
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Quản lý session không trạng thái cho JWT
                .authenticationProvider(authenticationProvider())  // Sử dụng provider xác thực tùy chỉnh
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)  // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
                .build();
    }
}

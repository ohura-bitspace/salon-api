package jp.bitspace.salon.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtCustomerAuthenticationFilter jwtCustomerAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護を無効化（REST APIでは通常無効にします）
            .csrf(csrf -> csrf.disable())
            // CORS（WebConfig の設定を使用）
            .cors(Customizer.withDefaults())
            // JWTなので、STATELESS
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 認可ルール
            .authorizeHttpRequests(auth -> auth
                // /me はトークン確認用のため認証必須
                .requestMatchers("/api/customer/auth/me").hasRole("CUSTOMER")
                .requestMatchers("/api/customer/auth/refresh").hasRole("CUSTOMER")
                // それ以外の認証系は認証不要
                .requestMatchers("/api/customer/auth/**").permitAll()
                .requestMatchers("/api/webhooks/**").permitAll()
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                // 既存（管理者含む）APIへの影響を避けるため、それ以外は現状通り許可
                .anyRequest().permitAll()
            )
            // 未認証は 401 を返す（顧客API向け）
            .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(jwtCustomerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

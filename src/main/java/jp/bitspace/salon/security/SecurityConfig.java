package jp.bitspace.salon.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF保護を無効化（REST APIでは通常無効にします）
            .csrf(csrf -> csrf.disable())
            // すべてのリクエストを許可（開発中のみ。後で制限します）
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        // TODO 年明けに認証系実装
        //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

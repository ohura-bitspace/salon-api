package jp.bitspace.salon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
	@Bean
	WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
                .allowedOrigins("http://localhost", "http://localhost:5173", "http://localhost:5174") 
                .allowedOriginPatterns("*") // ← 面倒なら開発中はこれでもOK（Spring Bootのバージョンによる）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedOriginPatterns("http://localhost:*") // 許可するオリジンを指定
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(true);
			}
		};
	}
}

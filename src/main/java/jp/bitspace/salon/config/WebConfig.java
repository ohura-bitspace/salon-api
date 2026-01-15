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
				// ↓↓↓ ここに ngrok のURLを追加するか、一時的に "*" (全許可) にする ↓↓↓
                .allowedOrigins("http://localhost", "http://localhost:5173", "http://localhost:5174", "https://kandace-icicled-unadmissibly.ngrok-free.dev") 
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

package jp.bitspace.salon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	@Bean
	WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
                .allowedOrigins("http://localhost", 
                				"http://localhost:5173", 
                				"http://localhost:5174",
                				"https://salon.bitspace.jp") 
                .allowedOriginPatterns("http://localhost:*") // 許可するオリジンを指定
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 全てのヘッダーを許可
				.allowedHeaders("*")
				.allowCredentials(true);
			}
			
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				
				// パスの末尾に必ずスラッシュが付くように補正
			    String location = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
				
				// /uploads/** のパスで uploadDir ディレクトリの静的リソースを公開
				registry.addResourceHandler("/uploads/**")
					.addResourceLocations("file:" + location);
			}
		};
	}
}

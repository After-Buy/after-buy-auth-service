package com.After_Buy.AuthService.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * 전역 Swagger 통합 API 문서화 자동화 환경설정
 * 프론트엔드나 클라이언트 엔지니어들이 API 규격을 브라우저에서 편하게 구경 및 실험할 수 있도록 스웨거 UI의 메타데이터와 JWT 보안 스키마를 구성합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Auth Service API", version = "v1", description = "After-Buy Auth Service API Documentation"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
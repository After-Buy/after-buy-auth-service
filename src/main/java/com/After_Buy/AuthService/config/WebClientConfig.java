package com.After_Buy.AuthService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 전역 WebClient 싱글톤 빈 구성 세팅 파일
 * 카카오 인증 서버들이나 자사 내부 MSA 타 마이크로 서비스 서버들에게 논블로킹(Non-Blocking) 방식으로 HTTP 네트워크 요청을 쏠 수 있는 비동기 엔진을 주입합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Configuration
public class WebClientConfig {
    
    /**
     * WebClient Builder 전역 빈 생성 구문
     *
     * @return : 각 Web 통신 클라이언트 컴포넌트들에서 재사용할 WebClient 인스턴스 조립 공장 객체
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

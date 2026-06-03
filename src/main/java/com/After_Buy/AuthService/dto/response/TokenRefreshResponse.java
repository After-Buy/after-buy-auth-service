package com.After_Buy.AuthService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 재발급 응답 DTO
 * Refresh Token을 통해 새로 발급된 Access Token의 정보를 담아 반환하는 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@Builder
@AllArgsConstructor
public class TokenRefreshResponse {
    // 갱신된 새 Access Token
    private String accessToken;

    private String refreshToken;
    
    // 새 Access Token의 만료 시간
    private long expiresIn;
}

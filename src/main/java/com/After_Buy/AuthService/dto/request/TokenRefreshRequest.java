package com.After_Buy.AuthService.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Access Token 재발급 요청 DTO
 * 만료된 Access Token을 갱신하기 위해 클라이언트가 Refresh Token을 서버로 전송하는 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class TokenRefreshRequest {
    // 갱신을 위해 사용할 기존 Refresh Token
    @JsonProperty("refresh_token")
    @NotBlank(message="refresh_token은 필수입니다.")
    private String refreshToken;
}

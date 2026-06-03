package com.After_Buy.AuthService.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그아웃 요청 DTO
 * 사용자가 앱이나 웹에서 로그아웃 시 현재 기기의 Refresh Token을 넘겨주어 무효화시키기 위한 전송 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class LogoutRequest {
    // 로그아웃으로 폐기할 JWT Refresh Token
    @JsonProperty("refresh_token")
    @NotBlank(message="refresh_token은 필수입니다.")
    private String refreshToken;
}

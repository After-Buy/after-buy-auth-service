package com.After_Buy.AuthService.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 로그인 요청 DTO
 * 클라이언트에서 카카오 인가 코드와 리다이렉트 URI를 받아 로그인을 요청하기 위한 데이터 전송 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class KakaoLoginRequest {
    // 카카오 API로부터 받은 접근 인가 코드
    @NotBlank(message="인가 코드는 필수입니다.")
    private String authCode;

    // 카카오 로그인 후 리다이렉트 시킨 콜백 URI
    @NotBlank(message="redirect_uri는 필수입니다.")
    private String redirectUri;
}

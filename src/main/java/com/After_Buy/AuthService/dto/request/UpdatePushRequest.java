package com.After_Buy.AuthService.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 푸시 알림 수신 설정 변경 요청 DTO
 * 앱 내 푸시 알림 수신 동의 여부를 토글할 때 클라이언트에서 서버로 전송하는 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class UpdatePushRequest {
    // 변경할 알림 수신 동의 여부 상태값 (0: OFF, 1: ON)
    @NotNull(message="push_enabled는 필수입니다.")
    @Min(value=0L, message="push_enabled는 0 또는 1이어야 합니다.")
    @Max(value=1L, message="push_enabled는 0 또는 1이어야 합니다.")
    private Integer pushEnabled;
}

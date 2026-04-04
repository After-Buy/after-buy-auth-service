package com.After_Buy.AuthService.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청 DTO
 * 사용자가 서비스 탈퇴를 진행하기 전, 유의사항에 명시적으로 동의했는지 확인하기 위한 전송 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class WithdrawRequest {
    // 탈퇴 동의 여부 (true여야만 처리)
    @AssertTrue(message="탈퇴 동의는 필수입니다.")
    private boolean consent;
}

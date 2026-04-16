package com.After_Buy.AuthService.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 통계 정보 응답 DTO
 * 어드민 대시보드 등에서 사용할 목적으로 카운트된 유저 통계 수치를 담아 반환하는 객체
 * Admin Service와의 필드 호환성을 위해 @JsonProperty 설정 추가
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@Builder
@AllArgsConstructor
public class UserStatsResponse {
    
    @JsonProperty("total_users")
    private long totalUsers;

    @JsonProperty("total_users_prev_7d")
    private long totalUsersPrev7d;
    
    @JsonProperty("new_users_7d")
    private long newUsersCurrentPeriod;
    
    @JsonProperty("new_users_prev_7d")
    private long newUsersPrevPeriod;
}

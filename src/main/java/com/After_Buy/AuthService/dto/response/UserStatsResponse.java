package com.After_Buy.AuthService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 통계 정보 응답 DTO
 * 어드민 대시보드 등에서 사용할 목적으로 카운트된 유저 통계 수치를 담아 반환하는 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@Builder
@AllArgsConstructor
public class UserStatsResponse {
    // 앱 내 전체 누적 가입자 수 
    private long totalUsers;
    
    // 현재 기준 기간(최근 N일) 내에 가입한 신규 유저 수
    private long newUsersCurrentPeriod;
    
    // 이전 기준 기간(이전 N일) 내에 가입한 신규 유저 수 (증감률 비교용도)
    private long newUsersPrevPeriod;
}

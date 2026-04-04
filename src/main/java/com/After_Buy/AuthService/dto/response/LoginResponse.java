package com.After_Buy.AuthService.dto.response;

import com.After_Buy.AuthService.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 로그인 완료 응답 DTO
 * 정상적으로 인가되어 발급된 JWT 토큰 및 사용자의 기본 프로필 정보를 클라이언트에 반환하는 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    // 발급된 Access Token
    private String accessToken;
    
    // 발급된 Refresh Token
    private String refreshToken;
    
    // 토큰 타입 (보통 Bearer)
    private String tokenType;
    
    // Access Token 만료 시간 (밀리초 단위)
    private long expiresIn;
    
    // 접속한 유저의 기본 정보
    private UserInfo user;

    /**
     * 내부 영속성 엔티티 User를 응답용 UserInfo 형식으로 변환
     *
     * @param user : 데이터베이스에서 조회/저장된 유저 엔티티
     * @param isNewUser : 방금 새로 가입된 유저인지 여부
     * @return : 변환된 UserInfo 객체
     */
    public static UserInfo toUserInfo(User user, boolean isNewUser) {
        return UserInfo.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .pushEnabled(user.getPushEnabled())
                .createdAt(user.getCreatedAt())
                .isNewUser(isNewUser)
                .build();
    }

    /**
     * 응답에 포함될 사용자 상세 정보 중첩 클래스
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImageUrl;
        private int pushEnabled;
        private LocalDateTime createdAt;
        private boolean isNewUser;
    }
}

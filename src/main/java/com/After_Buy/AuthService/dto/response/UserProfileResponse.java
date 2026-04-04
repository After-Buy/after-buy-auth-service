package com.After_Buy.AuthService.dto.response;

import com.After_Buy.AuthService.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 사용자 프로필 상세 조회 응답 DTO
 * 내 프로필 조회 API 등 사용자의 세부 정보가 필요할 때 반환되는 전송 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String kakaoProviderId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private int pushEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * User 엔티티를 반환용 Profile Response DTO로 변환하는 정적 팩토리 메서드
     *
     * @param user : 영속성 컨텍스트에서 조회된 유저 엔티티
     * @return : 모든 정보가 매핑된 ProfileResponse
     */
    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .kakaoProviderId(user.getKakaoProviderId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .pushEnabled(user.getPushEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

package com.After_Buy.AuthService.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 수정 요청 DTO
 * 닉네임이나 프로필 사진을 변경하고자 할 때 클라이언트가 전달하는 데이터 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class UpdateProfileRequest {
    // 변경할 닉네임 (Null일 경우 기존 닉네임 유지)
    private String nickname;

    // 변경할 프로필 이미지 URL (Null일 경우 기존 프로필 이미지 유지)
    private String profileImageUrl;
}

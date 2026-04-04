package com.After_Buy.AuthService.controller;

import com.After_Buy.AuthService.dto.request.UpdateProfileRequest;
import com.After_Buy.AuthService.dto.request.UpdatePushRequest;
import com.After_Buy.AuthService.dto.response.ApiResponse;
import com.After_Buy.AuthService.dto.response.UserProfileResponse;
import com.After_Buy.AuthService.security.UserPrincipal;
import com.After_Buy.AuthService.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 프로필 제어 컨트롤러
 * 사용자가 자신의 인적 사항 및 푸시 알림 수신 상태 등 개인 프로필을 설정하고 볼 수 있는 기능들을 제공합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Tag(name="User Profile", description="사용자 프로필 API")
@RestController
@RequestMapping("/api/auth/users/me")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    /**
     * 유저 프로필 조회 메소드
     * 인가된 JWT 토큰을 기반으로 사용자 본인의 현재 등록된 기본 인적 정보 및 환경설정 상태를 열람합니다.
     *
     * @param principal : JWT 필터 단에서 파싱된 유저 상세 정보 객체
     * @return : 유저 식별자, 이메일, 닉네임, 알림 수신여부 등 프로필 데이터 모델 반환
     */
    @Operation(summary="내 프로필 조회", description="JWT에서 추출한 userId로 사용자 프로필을 조회합니다.", security={@SecurityRequirement(name="bearerAuth")})
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = this.userProfileService.getMyProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 속성 업데이트 메소드
     * 새로운 닉네임 문자열이나 업데이트된 스토리지 S3 이미지 URI 등을 받아 프로필 설정을 최신 상태로 바꿉니다.
     *
     * @param principal : JWT에서 추출해 주입된 접속 인증 유저 객체 정보
     * @param request   : 닉네임, 프로필 이미지 URL 등 변경될 속성 항목
     * @return : 업데이트 작업 종료 후 최신 상태가 반영된 프로필 DTO
     */
    @Operation(summary="프로필 수정", description="닉네임 또는 프로필 이미지 URL을 수정합니다.", security={@SecurityRequirement(name="bearerAuth")})
    @PatchMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = this.userProfileService.updateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 알림 상태 변경 및 동기화 메소드
     * 디바이스 앱 내에서 수집한 알림 설정 상태를 Auth Service에 맞추고 백엔드 내의 Notification 서비스와 동기화시킵니다.
     *
     * @param principal : 클라이언트 JWT에서 확인된 접속자 계정 식별자 정보
     * @param request   : 변경될 알림 수신 동의/거부 상태 값
     * @return : 처리 후 반영된 현재 푸시 알림 설정 값이 담긴 맵 형태의 응답 데이터
     */
    @Operation(summary="푸시 알림 설정 변경", description="알림 ON(1)/OFF(0)를 변경하고 알림 서비스와 동기화합니다.", security={@SecurityRequirement(name="bearerAuth")})
    @PatchMapping("/push")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> updatePushEnabled(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdatePushRequest request) {
        int updated = this.userProfileService.updatePushEnabled(principal.getUserId(), request.getPushEnabled());
        return ResponseEntity.ok(ApiResponse.success(Map.of("push_enabled", updated)));
    }
}

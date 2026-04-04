package com.After_Buy.AuthService.controller;

import com.After_Buy.AuthService.dto.request.KakaoLoginRequest;
import com.After_Buy.AuthService.dto.request.LogoutRequest;
import com.After_Buy.AuthService.dto.request.TokenRefreshRequest;
import com.After_Buy.AuthService.dto.request.WithdrawRequest;
import com.After_Buy.AuthService.dto.response.ApiResponse;
import com.After_Buy.AuthService.dto.response.LoginResponse;
import com.After_Buy.AuthService.dto.response.TokenRefreshResponse;
import com.After_Buy.AuthService.security.UserPrincipal;
import com.After_Buy.AuthService.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 및 인증 제어 컨트롤러
 * 사용자의 카카오 로그인, 토큰 재발급, 로그아웃, 회원탈퇴 등 앱의 인증 및 회원 흐름을 담당합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Tag(name="Authentication", description="사용자 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 카카오 로그인 및 자동 가입 메소드
     * 인가 코드를 받아 카카오 계정으로 시스템에 로그인하거나 최초 구동시 신규 가입 처리를 진행합니다.
     *
     * @param request : 클라이언트에서 전달하는 인가 코드 및 리다이렉트 URI 정보
     * @return : JWT (Access, Refresh) 및 회원 기본 정보가 포함된 공통 응답 DTO
     */
    @Operation(summary="카카오 로그인", description="카카오 인가 코드로 로그인 또는 자동 회원가입 처리")
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        LoginResponse response = this.authService.kakaoLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Access Token 재발급 메소드
     * 만료된 Access Token을 갱신하기 위해 유효한 Refresh Token을 검증하고 새로운 Access Token을 생성 및 반환합니다.
     *
     * @param request : 클라이언트가 전송하는 Refresh Token 정보
     * @return : 갱신된 새 Access Token 정보가 포함된 공통 응답 DTO
     */
    @Operation(summary="Access Token 재발급", description="Refresh Token으로 새 Access Token 발급")
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = this.authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 회원 로그아웃 메소드
     * 현재 기기의 Refresh Token을 폐기 처리하여 불필요한 세션을 해제하고 로그아웃 상태로 전환시킵니다.
     *
     * @param principal : JWT를 통해 주입된 로그인된 유저 인증 정보
     * @param request : 사용자 기기에서 로그아웃 처리를 하기 위한 Refresh Token 정보
     * @return : 로그아웃 완료 성공 메시지
     */
    @Operation(summary="로그아웃", description="현재 기기의 Refresh Token을 폐기합니다.", security={@SecurityRequirement(name="bearerAuth")})
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody LogoutRequest request) {
        this.authService.logout(principal.getUserId(), request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.successMessage("로그아웃되었습니다."));
    }

    /**
     * 회원 탈퇴 메소드
     * 사용자의 계정을 삭제하고, 이에 의존하는 기기 정보 및 알림 데이터를 내부 트랜잭션을 통해 영구적으로 삭제합니다.
     *
     * @param principal : JWT를 통해 주입된 로그인된 유저 인증 정보
     * @param request : 사용자의 탈퇴 동의 여부 정보
     * @return : 회원 탈퇴 완료 메시지
     */
    @Operation(summary="회원 탈퇴", description="사용자 계정 및 모든 관련 데이터를 영구 삭제합니다.", security={@SecurityRequirement(name="bearerAuth")})
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody WithdrawRequest request) {
        this.authService.withdraw(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.successMessage("회원 탈퇴가 완료되었습니다. 모든 데이터가 삭제되었습니다."));
    }
}

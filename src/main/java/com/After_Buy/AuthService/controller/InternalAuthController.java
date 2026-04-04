package com.After_Buy.AuthService.controller;

import com.After_Buy.AuthService.dto.response.UserStatsResponse;
import com.After_Buy.AuthService.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인프라 및 서비스 간 내부 통신 제어 컨트롤러
 * 클라이언트 엔드포인트가 아닌 MSA 아키텍처 상의 타 서비스(Admin Service 등)에서 자체 통신을 위해 사용합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Tag(name = "Internal Auth", description = "MSA 내부 통신 전용 Auth API (Admin -> Auth)")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final UserProfileService userProfileService;

    /**
     * 통계 데이터 반환 메소드
     * 관리자 대시보드 등의 용도로 회원 통계를 타 서비스에 전달하기 위해 호출되는 메소드입니다.
     *
     * @param days : 조회할 통계 기준 최근 일수 제한값 지정 (기본 7일)
     * @return : 전체 가입자 및 해당 기간 내의 신규 가입자 수가 담긴 통계 응답 객체
     */
    @Operation(summary = "[Internal] 사용자 통계",
               description = "전체 사용자 수 및 기간별 신규 가입자 수를 반환합니다. (Admin Service 내부 호출 전용)")
    @Parameter(name = "X-Internal-Secret", description = "내부 보안 키", required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @GetMapping("/users/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(userProfileService.getUserStats(days));
    }
}
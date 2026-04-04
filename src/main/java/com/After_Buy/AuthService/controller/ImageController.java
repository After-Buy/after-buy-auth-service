package com.After_Buy.AuthService.controller;

import com.After_Buy.AuthService.dto.request.PresignedUrlRequest;
import com.After_Buy.AuthService.dto.response.ApiResponse;
import com.After_Buy.AuthService.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * S3 이미지 파일 처리 제어 컨트롤러
 * 사용자의 프로필 이미지 업데이트 및 관리를 위해 S3 업로드용 임시 링크(Pre-signed URL)를 제공합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Tag(name="Image", description="프로필 이미지 S3 Pre-signed URL 발급 API")
@RestController
@RequestMapping("/api/auth/images")
public class ImageController {

    /**
     * 프로필 이미지 권한 임시 URL 발급 메소드
     * 클라이언트가 백엔드 서버를 거치지 않고 AWS S3에 직접 이미지 파일을 업로드할 수 있도록 짧은 만료 시간을 가진 URL을 발급합니다.
     *
     * @param principal : JWT를 통해 주입된 접속 유저 인증 정보
     * @param request : 서버 측 정책에 맞춰 업로드할 이미지 확장자 검증 정보
     * @return : 업로드 가능한 Pre-signed URL 및 저장된 이미지 URL이 담긴 데이터 정보
     */
    @Operation(summary="프로필 이미지 Pre-signed URL 발급 (Stub)", description="S3 직접 업로드용 Pre-signed URL 발급. 현재 AWS 미설정으로 Stub 응답 반환.", security={@SecurityRequirement(name="bearerAuth")})
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPresignedUrl(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody PresignedUrlRequest request) {
        String stubFileName = UUID.randomUUID() + "." + request.getFileExtension();
        String stubPresignedUrl = "https://stub-s3.amazonaws.com/after-buy/profiles/" + stubFileName + "?X-Amz-Signature=stub-signature";
        String stubImageUrl = "https://stub-s3.amazonaws.com/after-buy/profiles/" + stubFileName;
        
        log.warn("[Stub] Pre-signed URL 발급 (실제 S3 연동 필요): userId={}, extension={}", principal.getUserId(), request.getFileExtension());
        
        Map<String, Object> data = Map.of(
                "presigned_url", stubPresignedUrl,
                "image_url", stubImageUrl,
                "expires_in", 300
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}

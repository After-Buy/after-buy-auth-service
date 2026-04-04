package com.After_Buy.AuthService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * S3 이미지 업로드 권한용 Pre-signed URL 요청 DTO
 * 클라이언트가 백엔드를 거치지 않고 S3 스토리지에 직접 이미지를 업로드할 수 있도록 짧은 만료시간을 가진 URL을 발급받기 위한 객체
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Getter
@NoArgsConstructor
public class PresignedUrlRequest {
    // 업로드 할 대상 파일의 확장자 제약사항 (서버에서 패턴 검증)
    @NotBlank(message="file_extension은 필수입니다.")
    @Pattern(regexp="^(jpg|jpeg|png|webp)$", message="지원하지 않는 확장자입니다. jpg, jpeg, png, webp만 허용됩니다.")
    private String fileExtension;
}

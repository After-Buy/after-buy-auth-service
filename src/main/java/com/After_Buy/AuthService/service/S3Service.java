package com.After_Buy.AuthService.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * AWS S3 프로필 이미지 관리 서비스
 * Pre-signed URL 발급을 통해 클라이언트가 백엔드를 거치지 않고 S3에 직접 이미지를 업로드할 수 있도록 지원합니다.
 *
 * @since : 2026.05.05
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /** Pre-signed URL 만료 시간 (초) */
    private static final int PRESIGN_EXPIRATION_SECONDS = 300;

    /**
     * 프로필 이미지 업로드용 Pre-signed URL 발급
     * S3에 PUT 업로드가 가능한 임시 서명 URL과 업로드 완료 후 접근 가능한 이미지 URL을 생성합니다.
     *
     * @param userId        : 업로드 요청 사용자 식별자 (S3 경로 구분용)
     * @param fileExtension : 업로드할 이미지 파일 확장자 (png, jpg 등)
     * @return : presigned_url, image_url, expires_in이 담긴 Map
     */
    public Map<String, Object> generatePresignedUrl(Long userId, String fileExtension) {
        // S3 객체 키 생성: profiles/{userId}/{UUID}.{ext}
        String objectKey = "profiles/" + userId + "/" + UUID.randomUUID() + "." + fileExtension;

        // Content-Type 결정
        String contentType = resolveContentType(fileExtension);

        // PutObject 요청 설정
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        // Pre-signed URL 생성 (만료: 5분)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(PRESIGN_EXPIRATION_SECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        // 업로드 완료 후 접근 가능한 이미지 URL
        String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, objectKey);

        log.info("[S3] Pre-signed URL 발급 완료: userId={}, key={}, expires={}s", userId, objectKey, PRESIGN_EXPIRATION_SECONDS);

        return Map.of(
                "presigned_url", presignedUrl,
                "image_url", imageUrl,
                "expires_in", PRESIGN_EXPIRATION_SECONDS
        );
    }

    /**
     * 파일 확장자로부터 Content-Type MIME 타입을 결정합니다.
     *
     * @param extension : 파일 확장자 (jpg, jpeg, png, webp)
     * @return : 대응하는 MIME 타입 문자열
     */
    private String resolveContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}

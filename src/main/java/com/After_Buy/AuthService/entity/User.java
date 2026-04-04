package com.After_Buy.AuthService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 애프터바이(After-Buy) 통합 사용자 핵심 엔티티
 * 애플리케이션 플랫폼을 이용하는 회원들의 중심 정보(카카오 연결 상태, 닉네임, 이메일 등)를 MySQL 데이터베이스와 매핑하기 위한 JPA 객체입니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = "kakao_provider_id"),
    indexes = @Index(name = "idx_kakao_provider_id", columnList = "kakao_provider_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 모든 테이블 연관관계의 중심이 될 내부 고유 기본키(PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * 카카오 OAuth 서버에서 발급받은 유저 식별 시리얼 넘버 (유니크)
     */
    @Column(name = "kakao_provider_id", nullable = false, unique = true, length = 50)
    private String kakaoProviderId;

    /**
     * 알림 수신이나 복구 등에 활용될 개인 이메일 (선택 입력)
     */
    @Column(name = "email", nullable = true, length = 100)
    private String email;

    /**
     * 서비스 전반에서 표시될 회원의 활동 닉네임
     */
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    /**
     * S3나 카카오 저장소 등에 연결된 마이페이지 썸네일 파일 주소
     */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * 마케팅 및 시스템 푸시 알림 수신 활성화 상태 (1: ON, 0: OFF)
     */
    @Builder.Default
    @Column(name = "push_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private int pushEnabled = 1;

    /**
     * 마지막으로 로그인(Access Token 발급 및 리프레시 포함)한 시점 기록
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 최초 회원가입이 발생하여 레코드가 영구적으로 생성된 일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 프로필 사진이나 닉네임 변경 등으로 최신 업데이트가 일어난 일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
package com.After_Buy.AuthService.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * 로그인 세션 장기 유지를 위한 리프레시 토큰 관리 보안 엔티티
 * 클라이언트 기기에 발급된 보조 토큰들의 진위성을 데이터베이스상에서 검증하고 로그아웃, 회원탈퇴 시 접근 권한을 박탈시키기 위한 테이블 묶음입니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="refresh_tokens", indexes={
    @Index(name="idx_refresh_user_id", columnList="user_id"),
    @Index(name="idx_refresh_token_value", columnList="token_value(64)")
})
public class RefreshToken {

    /**
     * 데이터베이스 내부 처리를 위한 토큰의 일련 고유번호(PK)
     */
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="token_id")
    private Long tokenId;

    /**
     * 이 토큰의 진짜 주인이 되는 대상 회원 유저 객체 정보(N:1 다자 연결)
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false, foreignKey=@ForeignKey(name="fk_refresh_token_user"))
    private User user;

    /**
     * 발급 시 쿠키나 기기에 내려진 실제 해시 암호화 토큰 고유 텍스트
     */
    @Column(name="token_value", nullable=false, length=512)
    private String tokenValue;

    /**
     * 재설정 및 연장 권한이 소진되어 폐기되는 강제 파기자연 만료 날짜
     */
    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;

    /**
     * 토큰이 최초로 시스템에 등재된 로그인이 기점인 발급 날짜
     */
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    /**
     * 로그아웃이나 보안 무결성 이슈로 인해 인위적으로 수명 정지(무효화)를 시킨 파기 플래그
     */
    @Builder.Default
    @Column(name="revoked", nullable=false, columnDefinition="TINYINT(1) DEFAULT 0")
    private boolean revoked = false;
}

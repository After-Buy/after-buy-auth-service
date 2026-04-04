package com.After_Buy.AuthService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * auth_db.users ?뚯씠釉?留ㅽ븨 ?뷀떚?? * 移댁뭅??OAuth濡?媛?낇븳 ?ъ슜??怨꾩젙 ?뺣낫瑜?愿由ы븳??
 * ?뚯썝 ?덊눜 ??Hard Delete 泥섎━?쒕떎.
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

    /** ?ъ슜???대? 怨좎쑀 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** 移댁뭅??怨좎쑀 ?앸퀎??(Provider ID) */
    @Column(name = "kakao_provider_id", nullable = false, unique = true, length = 50)
    private String kakaoProviderId;

    /** 移댁뭅??怨꾩젙 ?대찓??(鍮꾩쫰?덉뒪 ???ъ궗 ?꾩뿉??誘몃룞???곹깭?????덉뼱 nullable) */
    @Column(name = "email", nullable = true, length = 100)
    private String email;

    /** ?됰꽕??(移댁뭅??湲곕낯媛? ?ъ슜??蹂寃?媛?? */
    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    /** ?꾨줈???대?吏 S3 URL */
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    /**
     * ?몄떆 ?뚮┝ ?섏떊 ?숈쓽 ?щ?
     * 1 = ON (湲곕낯媛?, 0 = OFF
     */
    @Builder.Default
    @Column(name = "push_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private int pushEnabled = 1;

    /** 留덉?留?濡쒓렇???쇱떆 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 怨꾩젙 ?앹꽦(媛?? ?쇱떆 ???먮룞 ?ㅼ젙 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** ?꾨줈??留덉?留??섏젙 ?쇱떆 ???먮룞 媛깆떊 */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
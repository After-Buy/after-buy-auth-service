package com.After_Buy.AuthService.repository;

import com.After_Buy.AuthService.entity.RefreshToken;
import com.After_Buy.AuthService.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 접속 및 세션 갱신 연장 처리를 조율하는 리프레시 토큰 제어망 리포지토리 인터페이스
 * 암호화된 토큰의 유무 및 정합성 폐기 여부를 체크하고 특정 상황에 맞춰 대량 무효 무력화 조치를 수행하는 보안 제어 목적의 데이터 관리 계층입니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 아직 폐기되지 않은 생존 활성 상태의 토큰을 문자열로 역순 조회하는 기능 필터
     *
     * @param tokenValue : 클라이언트가 Access 최신화를 위해 네트워크로 제시 제출한 서명 토큰
     * @return : 도난되거나 폐기된 이력 없이 정상 동작 중인 엔티티 발견 시의 래퍼 레코드
     */
    Optional<RefreshToken> findByTokenValueAndRevokedFalse(String tokenValue);

    /**
     * 로그인된 여러 기기에서 동시 접속 중인 유저의 모든 토큰들을 일괄로 강제 마비 파기 처리하는 질의
     * 비밀번호 누출 대응이나 징계, 다중 단말기 로그아웃 등 강력한 중앙 보안 조치를 위해 한 객체의 토큰 상태를 모두 철회시킵니다.
     *
     * @param user : 토큰의 생존 숨통을 끊어버릴 통제 목표물 타겟 유저 모델
     */
    @Modifying
    @Query(value="UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllByUser(@Param("user") User user);
}

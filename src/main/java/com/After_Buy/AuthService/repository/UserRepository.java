package com.After_Buy.AuthService.repository;

import com.After_Buy.AuthService.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 유저 기본 정보 CRUD 중심 JPA 리포지토리 인터페이스
 * 유저 정보의 탐색과 관리자 집계 통계 산출을 위해 자바 기본 질의어 및 커스텀 JPQL을 혼합하여 보관 운용하는 데이터 접근 계층입니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 카카오 제공자 고유 식별코드로 회원을 조회하는 질의 조건
     *
     * @param kakaoProviderId : 카카오 로그인 시 넘어온 OAuth 시리얼 넘버
     * @return : 일치하는 가입 회원이 존재할 경우를 대비한 널 세이프(Null-Safe) 래퍼 객체
     */
    Optional<User> findByKakaoProviderId(String kakaoProviderId);

    /**
     * 시작일과 종료일 사이(Between)의 신규 가입자 층수를 알아내는 통계 질의용 카운트
     *
     * @param start : 조회하고자 하는 날짜 범위의 구간 시작 지점
     * @param end : 조회하고자 하는 날짜 범위의 최신 마지막 지점
     * @return : 해당 기간(Days) 사이에 시스템에 가입한 누적 전체 유저 숫자
     */
    @Query(value="SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start AND u.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

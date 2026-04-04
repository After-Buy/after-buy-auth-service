package com.After_Buy.AuthService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 암호화 처리 및 발행/파싱 총괄 프로바이더
 * JJWT 라이브러리를 이용해 서버 서명 비밀키로 토큰을 찍어내거나 위변조 판단, 수명 등을 다루는 일급 인증 공장 역할을 수행합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * Access Token(가벼운 세션용) 생성 메소드
     * 인가된 사용자 ID를 주입해 클라이언트 측에서 매 API 요청 시 권한 증명으로 보낼 수 있는 짧은 만료 기한의 통행증을 만듭니다.
     *
     * @param userId : 통행증의 주인이 될 유저 데이터베이스 식별번호(PK)
     * @return : Base64 알고리즘과 비밀키로 연산된 최신 규격 JWT 토큰 문자열
     */
    public String generateAccessToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + this.accessExpiration))
                .signWith(this.secretKey)
                .compact();
    }

    /**
     * Refresh Token(긴 세션 보장용 갱신키) 생성 메소드
     * 액세스 토큰 만료 시 재발급의 근거가 될 긴 보안수명의 강력한 암호화된 토큰을 만들어 기기에 저장하도록 반환합니다.
     *
     * @param userId : 갱신권이 발행될 유저 식별값
     * @return : 오직 재발급과 로그아웃 폐기 로직에서만 제한적으로 활용될 발급 토큰 문자열
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + this.refreshExpiration))
                .signWith(this.secretKey)
                .compact();
    }

    /**
     * 정상 토큰 코어 데이터 복호화 디코드 처리
     * 검증 필터를 지난 외부 문자열 토큰의 서명 락을 풀어 주입되어있는 원래의 유저 ID 식별값을 강제로 추출해냅니다.
     *
     * @param token : 파싱을 시도할 암호 문자 원본
     * @return : 내재되어있던 유저의 데이터베이스 연동 PK 수동 반환
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 잔여 만료 한계 초 단위 역연산
     *
     * @param token : 생존 기한을 검사해볼 대상
     * @return : 토큰이 수명을 다해 멈추기 전까지 남은 현시점 기준 초(Seconds) 반환
     */
    public long getExpiresIn(String token) {
        Claims claims = parseClaims(token);
        long expMs = claims.getExpiration().getTime();
        return (expMs - System.currentTimeMillis()) / 1000L;
    }

    /**
     * 서명 및 형식 무결성 점검 보안 판독 기능
     * 위조, 변조되거나 기간이 지난 토큰을 잡아내어 불량 처리하기 위한 부울린 반환형 탐지기 역할을 수행합니다.
     *
     * @param token : 클레임 파싱 무결성 점검을 시도할 암호 문자
     * @return : 정상적인 수명의 조작 없는 깨끗한 토큰일 시 true, 불량품이거나 만료 시 false 리턴
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(this.secretKey).build().parseSignedClaims(token).getPayload();
    }

    public Date getRefreshTokenExpiry() {
        return new Date(System.currentTimeMillis() + this.refreshExpiration);
    }

    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }
}

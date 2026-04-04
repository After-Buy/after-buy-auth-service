package com.After_Buy.AuthService.service;

import com.After_Buy.AuthService.client.InternalDeviceClient;
import com.After_Buy.AuthService.client.InternalNotificationClient;
import com.After_Buy.AuthService.client.KakaoAuthClient;
import com.After_Buy.AuthService.dto.request.KakaoLoginRequest;
import com.After_Buy.AuthService.dto.response.LoginResponse;
import com.After_Buy.AuthService.dto.response.TokenRefreshResponse;
import com.After_Buy.AuthService.entity.RefreshToken;
import com.After_Buy.AuthService.entity.User;
import com.After_Buy.AuthService.exception.CustomException;
import com.After_Buy.AuthService.repository.RefreshTokenRepository;
import com.After_Buy.AuthService.repository.UserRepository;
import com.After_Buy.AuthService.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * 전역 인증 서비스 로직
 * 카카오 소셜 로그인을 기반으로 회원의 가입 및 액세스/리프레시 토큰의 발행 등 보안 처리의 핵심 역할을 수행합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoAuthClient kakaoAuthClient;
    private final InternalNotificationClient notificationClient;
    private final InternalDeviceClient deviceClient;

    /**
     * 카카오 로그인 및 자동 가입 시스템 프로세스
     * 카카오 측에서 보내준 인가 코드로 유저 정보를 분석한 뒤 신규 가입이 필요할 시 데이터베이스를 세팅하여 JWT 토큰 쌍을 발행하고 응답합니다.
     *
     * @param request : 클라이언트가 전달한 카카오 1회용 승인 토큰과 리다이렉트 URI 묶음
     * @return : 정상 발급된 액세스/리프레시 토큰 및 가입자의 세부 응답 모델 형태
     */
    @Transactional
    public LoginResponse kakaoLogin(KakaoLoginRequest request) {
        String kakaoAccessToken = this.kakaoAuthClient.exchangeToken(request.getAuthCode(), request.getRedirectUri());
        KakaoAuthClient.KakaoUserInfo userInfo = this.kakaoAuthClient.getUserInfo(kakaoAccessToken);
        Optional<User> existingUser = this.userRepository.findByKakaoProviderId(userInfo.kakaoId());
        boolean isNewUser = existingUser.isEmpty();
        User user = existingUser.orElseGet(() -> this.registerNewUser(userInfo));
        
        if (!isNewUser) {
            user.setLastLoginAt(LocalDateTime.now());
        }
        
        String accessToken = this.jwtTokenProvider.generateAccessToken(user.getUserId());
        String refreshToken = this.jwtTokenProvider.generateRefreshToken(user.getUserId());
        long expiresIn = this.jwtTokenProvider.getExpiresIn(accessToken);
        LocalDateTime expiresAt = this.jwtTokenProvider.getExpirationFromToken(refreshToken).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        this.refreshTokenRepository.save(RefreshToken.builder().user(user).tokenValue(refreshToken).expiresAt(expiresAt).build());
        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").expiresIn(expiresIn).user(LoginResponse.toUserInfo(user, isNewUser)).build();
    }

    /**
     * 카카오 로그인 환경의 신규 유저 초기 정보 세팅 보조 함수
     * 최초 로그인 시 계정을 로컬 DB 엔티티에 영속화시키고 푸시 동의 설정 등 협력 MSA 서비스로 계정 생성 신호를 통신합니다.
     *
     * @param userInfo : 카카오 OAuth에서 다운받아 정제된 가입 유저의 신상 정보
     * @return : 내부 데이터베이스에 INSERT 과정을 거친 실존 유저 데이터 모델
     */
    private User registerNewUser(KakaoAuthClient.KakaoUserInfo userInfo) {
        User newUser = User.builder().kakaoProviderId(userInfo.kakaoId()).email(userInfo.email()).nickname(userInfo.nickname()).profileImageUrl(userInfo.profileImageUrl()).lastLoginAt(LocalDateTime.now()).build();
        User savedUser = this.userRepository.save(newUser);
        this.notificationClient.initPushSettings(savedUser.getUserId(), 1);
        log.info("신규 사용자 가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return savedUser;
    }

    /**
     * 기간이 끝난 Access Token 최신화 및 만료기간 갱신 프로세스
     * 클라이언트가 제시한 Refresh Token 고유 문자의 진위와 삭제 여부를 대조한 뒤 이상이 없을 시 새 Access Token을 하사합니다.
     *
     * @param refreshTokenValue : 기존에 발급되어 클라이언트 쿠키/저장소에 내재되어 있던 인증 토큰 문자열
     * @return : 최신 보안 유효 시간을 가진 새 Access Token이 동봉된 전송 묶음
     * @throws CustomException : 토큰이 강제 폐기(로그아웃)되었거나 기한 초과 상태라 갱신 불가일 때 예외
     */
    @Transactional(readOnly=true)
    public TokenRefreshResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = this.refreshTokenRepository.findByTokenValueAndRevokedFalse(refreshTokenValue).orElseThrow(() -> CustomException.unauthorized("유효하지 않거나 폐기된 Refresh Token입니다. 재로그인이 필요합니다."));
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw CustomException.unauthorized("Refresh Token이 만료되었습니다. 재로그인이 필요합니다.");
        }
        Long userId = refreshToken.getUser().getUserId();
        String newAccessToken = this.jwtTokenProvider.generateAccessToken(userId);
        long expiresIn = this.jwtTokenProvider.getExpiresIn(newAccessToken);
        return TokenRefreshResponse.builder().accessToken(newAccessToken).expiresIn(expiresIn).build();
    }

    /**
     * 접속 클라이언트 장치상의 회원 단반향 로그아웃 제어 로직
     * 로그아웃 트리거 진행 시 기기 브라우저 혹은 앱에 물리적으로 귀속된 Refresh Token 객체의 상태를 철회시킵니다.
     *
     * @param userId : 접속 중인 회원의 신상 정보를 입증하는 데이터베이스 PK
     * @param refreshTokenValue : 현 기기에서 탈취 방지 목적으로 보유 중이던 해제 대상 Refresh Token 원본
     * @throws CustomException : 다른 사람이 토큰을 도용하거나 발급 주인이 일치하지 않는 토큰 소거 요구 시 에러 반환
     */
    @Transactional
    public void logout(Long userId, String refreshTokenValue) {
        RefreshToken refreshToken = this.refreshTokenRepository.findByTokenValueAndRevokedFalse(refreshTokenValue).orElseThrow(() -> CustomException.unauthorized("유효하지 않은 Refresh Token입니다."));
        if (!refreshToken.getUser().getUserId().equals(userId)) {
            throw CustomException.unauthorized("토큰 소유자가 일치하지 않습니다.");
        }
        refreshToken.setRevoked(true);
        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * 소프트 딜리트가 아닌 회원 계정 물리적 증발 관제 트랜잭션 함수
     * 계정 탈퇴 요청 성사 시 유저 테이블의 Row 객체들을 제거할 뿐 아니라 알림, 기기 등의 타 마이크로서비스 연관 요소들마저 씻어냅니다.
     *
     * @param userId : 우주에서 지워야 하는 탈퇴 타겟 회원의 PK 아이디 넘버
     * @throws CustomException : 아예 없는 사용자를 겨냥해 탈퇴를 시도하는 무의미한 행위 조기 차단용 에러
     */
    @Transactional
    public void withdraw(Long userId) {
        User user = this.userRepository.findById(userId).orElseThrow(() -> CustomException.notFound("존재하지 않는 사용자입니다."));
        this.deviceClient.deleteUserDevices(userId);
        this.notificationClient.deleteUserNotifications(userId);
        this.userRepository.delete(user);
        log.info("회원 탈퇴 완료: userId={}", userId);
    }
}

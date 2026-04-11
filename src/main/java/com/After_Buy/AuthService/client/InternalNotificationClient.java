package com.After_Buy.AuthService.client;

import com.After_Buy.AuthService.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * 알림 서비스(Notification Service) 통신 클라이언트
 * 알림 설정값을 변경하거나 회원 탈퇴 시 알림 데이터를 청소하기 위해 연결할 때 사용합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Component
public class InternalNotificationClient {

    private final WebClient webClient;

    public InternalNotificationClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.notification-url}") String notificationUrl,
            @Value("${internal.secret-key}") String internalSecretKey) {
        this.webClient = webClientBuilder
                .baseUrl(notificationUrl)
                .defaultHeader("X-Internal-Secret", internalSecretKey)
                .build();
    }

    /**
     * 알림 상세 세팅(Settings) 초기화 요청 (비동기 Fire & Forget)
     * 최초 회원가입한 유저에게 시스템 상 기본 적용될 알림 권한값을 다른 서비스에 통보하여 뼈대를 만듭니다.
     * Notification Service 일시 다운 시에도 회원가입 트랜잭션에 영향을 주지 않습니다.
     * 실패 시 에러 로그만 기록하며, Notification Service의 Lazy Initialization이 추후 복구를 담당합니다.
     *
     * @param userId : 알람 수신 동의 상태를 적용할 유저 식별값
     * @param pushEnabled : 초기 푸시 수신 동의 상태 (1: ON, 0: OFF)
     */
    public void initPushSettings(Long userId, int pushEnabled) {
        webClient.post()
                .uri("/internal/push-settings/init")
                .bodyValue(Map.of("user_id", userId, "push_enabled", pushEnabled))
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                    result -> log.info("push_settings 초기 설정 생성 완료: userId={}", userId),
                    error  -> log.error("Notification Service 연결 실패 (init): userId={}, error={}", userId, error.getMessage())
                );
    }

    /**
     * 알림 상태값 동기화(Update) 요청
     * 프로필 세팅 화면에서 토글 버튼을 통해 알림 상태를 바꾼 경우, 타 서비스에 상태를 위임 동기화합니다.
     *
     * @param userId : 설정을 바꾼 유저 계정의 ID
     * @param pushEnabled : 새로 조작된 수신 상태값 (1: ON, 0: OFF)
     * @throws CustomException : 연결 오류 혹은 처리 실패 시 500에러 반환
     */
    public void syncPushSettings(Long userId, int pushEnabled) {
        try {
            webClient.post()
                    .uri("/internal/push-settings/sync")
                    .bodyValue(Map.of("user_id", userId, "push_enabled", pushEnabled))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("push_settings 동기화 완료: userId={}, pushEnabled={}", userId, pushEnabled);
        } catch (WebClientResponseException e) {
            log.error("push_settings sync 실패: userId={}, status={}", userId, e.getStatusCode());
            throw CustomException.internalError("푸시 알림 설정 동기화에 실패했습니다.");
        } catch (Exception e) {
            log.error("Notification Service 연결 실패 (sync): userId={}, error={}", userId, e.getMessage());
            throw CustomException.internalError("푸시 알림 서비스에 연결할 수 없습니다.");
        }
    }

    /**
     * 알림 이력 물리적 삭제 요청 (비동기 Fire & Forget)
     * 계정이 더 이상 유효하지 않은 환경(탈퇴 등)에서 푸시 메시지 흔적들을 영구히 폐기합니다.
     * 탈퇴의 핵심 목적은 auth_db.users 레코드 삭제이므로, Notification Service 일시 장애로 인해
     * 탈퇴 자체가 실패해서는 안 됩니다. 실패 시 에러 로그만 기록합니다.
     *
     * @param userId : 탈퇴 과정 중인 회원의 고유 ID
     */
    public void deleteUserNotifications(Long userId) {
        webClient.delete()
                .uri("/internal/notifications/users/{userId}", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                    result -> log.info("알림 데이터 삭제 완료: userId={}", userId),
                    error  -> log.error("Notification Service 연결 실패 (delete): userId={}, error={}", userId, error.getMessage())
                );
    }
}
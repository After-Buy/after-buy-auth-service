package com.After_Buy.AuthService.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * 디바이스 서비스(Device Service) 내부 통신 클라이언트
 * Device-Service 마이크로서비스에 데이터를 동기화하거나 영구 삭제를 비동기적으로 요청할 때 사용합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Component
public class InternalDeviceClient {
    private final WebClient webClient;

    public InternalDeviceClient(
            WebClient.Builder webClientBuilder,
            @Value("${services.device-url}") String deviceUrl,
            @Value("${internal.secret-key}") String internalSecretKey) {
        this.webClient = webClientBuilder
                .baseUrl(deviceUrl)
                .defaultHeader("X-Internal-Secret", internalSecretKey)
                .build();
    }

    /**
     * 회원의 가입된 기기(Device) 데이터 전체 삭제 요청 메서드 (비동기 Fire & Forget)
     * 회원이 탈퇴를 진행할 경우 Device Service 쪽에 보관된 해당 유저의 기기 데이터를 폐기하도록 요청합니다.
     * 탈퇴의 핵심 목적은 auth_db.users 레코드 삭제이므로, Device Service 일시 장애로 인해
     * 탈퇴 자체가 실패해서는 안 됩니다. 실패 시 에러 로그만 기록합니다.
     *
     * @param userId : 삭제할 대상이 되는 소유자의 사용자 PK
     */
    public void deleteUserDevices(Long userId) {
        this.webClient.delete()
                .uri("/internal/devices/users/{userId}", userId)
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(
                    result -> log.info("기기 데이터 삭제 완료: userId={}", userId),
                    error  -> log.error("Device Service 연결 실패: userId={}, error={}", userId, error.getMessage())
                );
    }
}

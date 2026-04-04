package com.After_Buy.AuthService.client;

import com.After_Buy.AuthService.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
     * 회원의 가입된 기기(Device) 데이터 전체 삭제 요청 메소드
     * 회원이 회원 탈퇴를 진행할 경우 Device Service 쪽에 보관된 해당 유저의 스마트폰 기기 토큰 등을 즉각 폐기하도록 통신합니다.
     *
     * @param userId : 삭제할 대상이 되는 소유자의 사용자 PK
     * @throws CustomException : 대상 서버가 응답 실패 상태를 반환하거나 접속 불가능할 경우 시스템 예외 발생
     */
    public void deleteUserDevices(Long userId) {
        try {
            this.webClient.delete()
                    .uri("/internal/devices/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("기기 데이터 삭제 완료: userId={}", userId);
        } catch (WebClientResponseException e) {
            log.error("기기 데이터 삭제 실패: userId={}, status={}", userId, e.getStatusCode());
            throw CustomException.internalError("기기 데이터 삭제에 실패했습니다.");
        } catch (Exception e) {
            log.error("Device Service 연결 실패: userId={}, error={}", userId, e.getMessage());
            throw CustomException.internalError("기기 서비스에 연결할 수 없습니다.");
        }
    }
}

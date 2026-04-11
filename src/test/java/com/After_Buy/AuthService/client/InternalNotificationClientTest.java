package com.After_Buy.AuthService.client;

import com.After_Buy.AuthService.exception.CustomException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InternalNotificationClient 비동기 전환 단위 테스트
 *
 * 검증 목적:
 * - initPushSettings(), deleteUserNotifications(): 비동기(Fire & Forget)
 *   → Notification Service 다운 시에도 예외 없이 정상 반환되는지 확인
 * - syncPushSettings(): 동기 유지
 *   → Notification Service 다운 시 CustomException이 던져지는지 확인
 *
 * @author 신태훈
 * @since 2026.04.11
 */
class InternalNotificationClientTest {

    private MockWebServer mockWebServer;
    private InternalNotificationClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        // WebClient.Builder를 직접 생성하여 MockWebServer URL로 연결
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Secret", "test-secret")
                .build();

        // 리플렉션 없이 테스트용 생성자 패턴 사용 (필드 직접 주입)
        client = new InternalNotificationClientTestHelper(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    // ==========================================================================
    // 1. initPushSettings() — 비동기 Fire & Forget 검증
    // ==========================================================================

    @Test
    @DisplayName("initPushSettings: Notification Service 정상 응답 → 예외 없이 완료")
    void initPushSettings_서버정상_예외없음() throws InterruptedException {
        // given: 서버가 201 Created 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(201)
                .setBody("{\"created\": true, \"user_id\": 1}")
                .addHeader("Content-Type", "application/json"));

        CountDownLatch latch = new CountDownLatch(1);

        // when: 비동기 호출 — 반환 즉시
        long startMs = System.currentTimeMillis();
        client.initPushSettings(1L, 1);
        long elapsedMs = System.currentTimeMillis() - startMs;

        // then: 즉시 반환(블로킹 없음) & 예외 없음
        latch.await(1, TimeUnit.SECONDS); // 비동기 완료 대기
        assertThat(elapsedMs).isLessThan(500); // 500ms 이내 반환 → 블로킹이 아님을 증명
    }

    @Test
    @DisplayName("initPushSettings: Notification Service 다운(연결 불가) → 예외 없이 완료 (Fire & Forget)")
    void initPushSettings_서버다운_예외없음() throws InterruptedException {
        // given: MockWebServer를 종료하여 연결 불가 상태 시뮬레이션
        try {
            mockWebServer.shutdown();
        } catch (IOException ignored) {}

        // when & then: 연결 실패해도 예외가 발생하지 않아야 함
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.initPushSettings(1L, 1);
        }, "비동기 Fire & Forget 방식이므로 서버 다운 시에도 예외가 발생하면 안 됩니다.");

        // 비동기 에러 콜백이 실행될 시간 확보
        Thread.sleep(300);
    }

    @Test
    @DisplayName("initPushSettings: Notification Service 500 에러 응답 → 예외 없이 완료")
    void initPushSettings_서버500에러_예외없음() throws InterruptedException {
        // given: 서버가 500 에러 반환
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // when & then: 500 에러에도 예외 발생 없음
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.initPushSettings(1L, 1);
        }, "비동기 방식에서 HTTP 500 에러는 에러 로그만 기록해야 합니다.");

        Thread.sleep(300);
    }

    // ==========================================================================
    // 2. deleteUserNotifications() — 비동기 Fire & Forget 검증
    // ==========================================================================

    @Test
    @DisplayName("deleteUserNotifications: Notification Service 정상 응답 → 예외 없이 완료")
    void deleteUserNotifications_서버정상_예외없음() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"deleted\": true, \"user_id\": 5}")
                .addHeader("Content-Type", "application/json"));

        // when & then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.deleteUserNotifications(5L);
        });

        Thread.sleep(300);
    }

    @Test
    @DisplayName("deleteUserNotifications: Notification Service 다운 → 탈퇴 트랜잭션에 영향 없음 (예외 없음)")
    void deleteUserNotifications_서버다운_예외없음() throws InterruptedException {
        // given: 서버 종료
        try {
            mockWebServer.shutdown();
        } catch (IOException ignored) {}

        // when & then: 서버 다운 시에도 탈퇴 흐름을 막으면 안 됨
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.deleteUserNotifications(5L);
        }, "비동기 Fire & Forget 방식이므로 서버 다운 시 탈퇴가 중단되어서는 안 됩니다.");

        Thread.sleep(300);
    }

    // ==========================================================================
    // 3. syncPushSettings() — 동기 유지 검증
    // ==========================================================================

    @Test
    @DisplayName("syncPushSettings: Notification Service 정상 응답 → 정상 완료")
    void syncPushSettings_서버정상_정상완료() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"synced\": true}")
                .addHeader("Content-Type", "application/json"));

        // when & then: 예외 없이 정상 완료
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.syncPushSettings(1L, 0);
        });
    }

    @Test
    @DisplayName("syncPushSettings: Notification Service 다운 → CustomException 발생 (동기 유지)")
    void syncPushSettings_서버다운_CustomException발생() throws IOException {
        // given: 서버 종료
        mockWebServer.shutdown();

        // when & then: 동기 방식이므로 연결 실패 시 CustomException 발생해야 함
        assertThatThrownBy(() -> {
            client.syncPushSettings(1L, 0);
        }).isInstanceOf(CustomException.class)
          .hasMessageContaining("푸시 알림");
    }

    // ==========================================================================
    // 테스트용 Inner 헬퍼 클래스 — WebClient를 직접 주입받는 패키지 전용 생성자
    // ==========================================================================

    /**
     * 테스트에서 WebClient를 직접 주입하기 위한 헬퍼 서브클래스
     * 실제 @Value 기반 생성자 대신 WebClient를 바로 받습니다.
     */
    static class InternalNotificationClientTestHelper extends InternalNotificationClient {

        InternalNotificationClientTestHelper(WebClient webClient) {
            // 실제 생성자 호출을 우회하기 위해 부모 생성자를 빈 값으로 호출 후
            // 리플렉션으로 webClient 필드 교체
            super(WebClient.builder(), "http://localhost:0", "test-secret");
            try {
                var field = InternalNotificationClient.class.getDeclaredField("webClient");
                field.setAccessible(true);
                field.set(this, webClient);
            } catch (Exception e) {
                throw new RuntimeException("테스트 WebClient 주입 실패", e);
            }
        }
    }
}

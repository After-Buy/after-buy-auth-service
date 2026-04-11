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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InternalDeviceClient 비동기 전환 단위 테스트
 *
 * 검증 목적:
 * - deleteUserDevices(): 비동기(Fire & Forget)
 *   → Device Service 다운 시에도 예외 없이 정상 반환되는지 확인
 *   → 기존 동기 방식에서는 CustomException이 던져졌으나, 비동기 전환 후에는 발생하지 않아야 함
 *
 * @author 신태훈
 * @since 2026.04.11
 */
class InternalDeviceClientTest {

    private MockWebServer mockWebServer;
    private InternalDeviceClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Secret", "test-secret")
                .build();

        client = new InternalDeviceClientTestHelper(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        try {
            mockWebServer.shutdown();
        } catch (Exception ignored) {}
    }

    // ==========================================================================
    // deleteUserDevices() — 비동기 Fire & Forget 검증
    // ==========================================================================

    @Test
    @DisplayName("deleteUserDevices: Device Service 정상 응답 → 예외 없이 완료")
    void deleteUserDevices_서버정상_예외없음() throws InterruptedException {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"deleted\": true, \"user_id\": 5}")
                .addHeader("Content-Type", "application/json"));

        // when & then
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.deleteUserDevices(5L);
        });

        Thread.sleep(300);
    }

    @Test
    @DisplayName("deleteUserDevices: Device Service 다운 → 탈퇴 트랜잭션에 영향 없음 (예외 없음)")
    void deleteUserDevices_서버다운_예외없음() throws InterruptedException, IOException {
        // given: 서버 종료 (연결 불가 시뮬레이션)
        mockWebServer.shutdown();

        // when & then: 서버 다운 시에도 탈퇴 흐름을 막으면 안 됨
        // 기존 동기 방식: CustomException 발생 → 탈퇴 실패
        // 변경 후 비동기 방식: 예외 없음 → 탈퇴 성공, 에러는 로그로만 기록
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.deleteUserDevices(5L);
        }, "비동기 Fire & Forget 방식이므로 Device Service 다운 시에도 탈퇴가 중단되어서는 안 됩니다.");

        Thread.sleep(300);
    }

    @Test
    @DisplayName("deleteUserDevices: Device Service 500 에러 → 예외 없이 완료 (에러 로그만 기록)")
    void deleteUserDevices_서버500에러_예외없음() throws InterruptedException {
        // given: 서버가 500 에러 반환
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // when & then: 500 에러에도 예외 발생 없음
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            client.deleteUserDevices(5L);
        }, "비동기 방식에서 HTTP 500 에러는 에러 로그만 기록하고 예외가 발생하면 안 됩니다.");

        Thread.sleep(300);
    }

    @Test
    @DisplayName("deleteUserDevices: 비동기 호출 → 즉시 반환 (블로킹 없음 검증)")
    void deleteUserDevices_비동기_즉시반환() throws InterruptedException {
        // given: 응답 지연 없이 즉시 응답
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"deleted\": true, \"user_id\": 5}")
                .addHeader("Content-Type", "application/json"));

        // when
        long startMs = System.currentTimeMillis();
        client.deleteUserDevices(5L);
        long elapsedMs = System.currentTimeMillis() - startMs;

        // then: 즉시 반환 (블로킹이 아님을 증명)
        org.junit.jupiter.api.Assertions.assertTrue(
            elapsedMs < 500,
            "비동기 호출이므로 500ms 이내에 반환되어야 합니다. 실제 소요: " + elapsedMs + "ms"
        );

        Thread.sleep(300);
    }

    // ==========================================================================
    // 테스트용 Inner 헬퍼 클래스
    // ==========================================================================

    /**
     * 테스트에서 WebClient를 직접 주입하기 위한 헬퍼 서브클래스
     */
    static class InternalDeviceClientTestHelper extends InternalDeviceClient {

        InternalDeviceClientTestHelper(WebClient webClient) {
            super(WebClient.builder(), "http://localhost:0", "test-secret");
            try {
                var field = InternalDeviceClient.class.getDeclaredField("webClient");
                field.setAccessible(true);
                field.set(this, webClient);
            } catch (Exception e) {
                throw new RuntimeException("테스트 WebClient 주입 실패", e);
            }
        }
    }
}

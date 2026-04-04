package com.After_Buy.AuthService.client;

import com.After_Buy.AuthService.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Notification Service ?대? ?듭떊 ?대씪?댁뼵??(Auth ??Notification)
 * Docker ?대? ?ㅽ듃?뚰겕 ?듭떊, X-Internal-Secret ?ㅻ뜑 ?ъ슜
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
     * ?좉퇋 媛????push_settings 珥덇린 ?덉퐫???앹꽦
     * POST /internal/push-settings/init
     *
     * @param userId      ?좉퇋 ?ъ슜??ID
     * @param pushEnabled 珥덇린 ?뚮┝ ?섏떊 ?숈쓽 (1=ON)
     * @throws CustomException Notification Service ?몄텧 ?ㅽ뙣 ??     */
    public void initPushSettings(Long userId, int pushEnabled) {
        try {
            webClient.post()
                    .uri("/internal/push-settings/init")
                    .bodyValue(Map.of("user_id", userId, "push_enabled", pushEnabled))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("push_settings 珥덇린 ?덉퐫???앹꽦 ?꾨즺: userId={}", userId);
        } catch (WebClientResponseException e) {
            log.error("push_settings init ?ㅽ뙣: userId={}, status={}", userId, e.getStatusCode());
            // 濡쒖뺄 ?뚯뒪?몃? ?꾪빐 ?덉쇅 ?섏?吏 ?딆쓬
            // throw CustomException.internalError("?뚮┝ ?쒕퉬??珥덇린?붿뿉 ?ㅽ뙣?덉뒿?덈떎. 媛?낆쓣 濡ㅻ갚?⑸땲??");
        } catch (Exception e) {
            log.error("Notification Service ?곌껐 ?ㅽ뙣 (init): userId={}, error={}", userId, e.getMessage());
            // 濡쒖뺄 ?뚯뒪?몃? ?꾪빐 ?덉쇅 ?섏?吏 ?딆쓬
            // throw CustomException.internalError("?뚮┝ ?쒕퉬?ㅼ뿉 ?곌껐?????놁뒿?덈떎. 媛?낆쓣 濡ㅻ갚?⑸땲??");
        }
    }

    /**
     * ?몄떆 ?뚮┝ ?섏떊 ?숈쓽 ?숆린??     * POST /internal/push-settings/sync
     *
     * @param userId      ?ъ슜??ID
     * @param pushEnabled 蹂寃쎈맂 ?숈쓽 ?щ? (0 or 1)
     * @throws CustomException Notification Service ?숆린???ㅽ뙣 ??     */
    public void syncPushSettings(Long userId, int pushEnabled) {
        try {
            webClient.post()
                    .uri("/internal/push-settings/sync")
                    .bodyValue(Map.of("user_id", userId, "push_enabled", pushEnabled))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("push_settings ?숆린???꾨즺: userId={}, pushEnabled={}", userId, pushEnabled);
        } catch (WebClientResponseException e) {
            log.error("push_settings sync ?ㅽ뙣: userId={}, status={}", userId, e.getStatusCode());
            throw CustomException.internalError("?뚮┝ ?ㅼ젙 ?숆린?붿뿉 ?ㅽ뙣?덉뒿?덈떎.");
        } catch (Exception e) {
            log.error("Notification Service ?곌껐 ?ㅽ뙣 (sync): userId={}, error={}", userId, e.getMessage());
            throw CustomException.internalError("?뚮┝ ?쒕퉬?ㅼ뿉 ?곌껐?????놁뒿?덈떎.");
        }
    }

    /**
     * ?뚯썝 ?덊눜 ???ъ슜???뚮┝ ?곗씠???꾩껜 ??젣
     * DELETE /internal/notifications/users/{userId}
     *
     * @param userId ?덊눜 ?ъ슜??ID
     * @throws CustomException ??젣 ?ㅽ뙣 ??     */
    public void deleteUserNotifications(Long userId) {
        try {
            webClient.delete()
                    .uri("/internal/notifications/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("?뚮┝ ?곗씠????젣 ?꾨즺: userId={}", userId);
        } catch (WebClientResponseException e) {
            log.error("?뚮┝ ?곗씠????젣 ?ㅽ뙣: userId={}, status={}", userId, e.getStatusCode());
            throw CustomException.internalError("?뚮┝ ?곗씠????젣???ㅽ뙣?덉뒿?덈떎.");
        } catch (Exception e) {
            log.error("Notification Service ?곌껐 ?ㅽ뙣 (delete): userId={}, error={}", userId, e.getMessage());
            throw CustomException.internalError("?뚮┝ ?쒕퉬?ㅼ뿉 ?곌껐?????놁뒿?덈떎.");
        }
    }
}
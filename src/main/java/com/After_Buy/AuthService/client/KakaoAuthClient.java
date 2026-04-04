package com.After_Buy.AuthService.client;

import com.After_Buy.AuthService.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * 카카오 인증 서버 API 통신 클라이언트
 * 카카오 자체 OAUTH 시스템과의 프로토콜을 수행하여 코드를 발급받고 액세스 토큰 교환 및 계정 데이터를 탈취(공유)받는 역할을 담당합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Component
public class KakaoAuthClient {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUrl;
    private final String userInfoUrl;

    public KakaoAuthClient(
            WebClient.Builder webClientBuilder,
            @Value("${kakao.client-id}") String clientId,
            @Value("${kakao.client-secret}") String clientSecret,
            @Value("${kakao.token-url}") String tokenUrl,
            @Value("${kakao.user-info-url}") String userInfoUrl) {
        this.webClient = webClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUrl = tokenUrl;
        this.userInfoUrl = userInfoUrl;
    }

    /**
     * 인가 코드를 바탕으로 카카오 API 규격의 Access Token으로 환전하는 메소드
     *
     * @param authCode : 클라이언트로부터 넘겨받은 1회성 카카오 승인 인가 코드
     * @param redirectUri : 로그인 시 설정했던 콜백 URI 무결성 검증 용도
     * @return : 카카오 세션으로 인증 인가된 텍스트 기반 Access Token
     * @throws CustomException : 인가 코드 유효만료나 교환 거절 시 예외 알림 처리
     */
    public String exchangeToken(String authCode, String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", authCode);
        if (clientSecret != null && !clientSecret.isEmpty()) {
            formData.add("client_secret", clientSecret);
        }

        try {
            Map<?, ?> response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("access_token") == null) {
                throw CustomException.unauthorized("카카오 Access Token 교환에 실패했습니다.");
            }
            return (String) response.get("access_token");

        } catch (WebClientResponseException e) {
            log.error("카카오 토큰 교환 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw CustomException.unauthorized("카카오 인가 코드가 유효하지 않습니다.");
        }
    }

    /**
     * 보유한 카카오 토큰을 사용해 사용자의 카카오톡 회원 정보 리스트를 요청하는 메소드
     *
     * @param kakaoAccessToken : 사전에 환전 발급된 유효한 사용자 접근용 OAUTH 토큰
     * @return : 닉네임, 카카오 고유 시리얼 아이디, 공개 사진 이메일 등 객체 모델
     * @throws CustomException : 폐기되거나 만료된 토큰으로 인한 카카오 정보 제공 거부 시 접근 반려
     */
    public KakaoUserInfo getUserInfo(String kakaoAccessToken) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri(userInfoUrl)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw CustomException.unauthorized("카카오 사용자 정보를 가져올 수 없습니다.");
            }

            String kakaoId = String.valueOf(response.get("id"));

            Map<?, ?> kakaoAccount = (Map<?, ?>) response.get("kakao_account");
            Map<?, ?> profile = kakaoAccount != null ? (Map<?, ?>) kakaoAccount.get("profile") : null;

            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            if (email != null && email.isEmpty()) {
                email = null;
            }

            String nickname = profile != null ? (String) profile.get("nickname") : "사용자";
            String profileImageUrl = profile != null ? (String) profile.get("profile_image_url") : null;

            return new KakaoUserInfo(kakaoId, email, nickname, profileImageUrl);

        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("카카오 사용자 정보 조회 실패: status={}", e.getStatusCode());
            throw CustomException.unauthorized("카카오 사용자 정보 조회에 실패했습니다.");
        }
    }

    /**
     * 카카오에서 건네준 복잡한 Json 트리를 파싱해 가볍게 담아낼 레코드
     */
    public record KakaoUserInfo(
            String kakaoId,
            String email,
            String nickname,
            String profileImageUrl
    ) {}
}
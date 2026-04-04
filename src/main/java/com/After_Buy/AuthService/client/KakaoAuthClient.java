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
 * 移댁뭅???몃? API ?몄텧 ?대씪?댁뼵??(WebClient 湲곕컲)
 * - ?멸? 肄붾뱶 ??Access Token 援먰솚
 * - Access Token ???ъ슜???꾨줈??議고쉶
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
     * 移댁뭅???멸? 肄붾뱶 ??Access Token 援먰솚
     *
     * @param authCode    ?멸? 肄붾뱶
     * @param redirectUri 由щ떎?대젆??URI
     * @return 移댁뭅??Access Token
     * @throws CustomException ?멸? 肄붾뱶媛 ?좏슚?섏? ?딆? 寃쎌슦 401
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
                throw CustomException.unauthorized("移댁뭅??Access Token 援먰솚???ㅽ뙣?덉뒿?덈떎.");
            }
            return (String) response.get("access_token");

        } catch (WebClientResponseException e) {
            log.error("移댁뭅???좏겙 援먰솚 ?ㅽ뙣: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw CustomException.unauthorized("移댁뭅???멸? 肄붾뱶媛 ?좏슚?섏? ?딆뒿?덈떎.");
        }
    }

    /**
     * 移댁뭅??Access Token?쇰줈 ?ъ슜???뺣낫 議고쉶
     *
     * @param kakaoAccessToken 移댁뭅??Access Token
     * @return KakaoUserInfo (kakaoId, email, nickname, profileImageUrl)
     * @throws CustomException ?ъ슜???뺣낫 議고쉶 ?ㅽ뙣 ??401
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
                throw CustomException.unauthorized("移댁뭅???ъ슜???뺣낫瑜?媛?몄삱 ???놁뒿?덈떎.");
            }

            String kakaoId = String.valueOf(response.get("id"));

            Map<?, ?> kakaoAccount = (Map<?, ?>) response.get("kakao_account");
            Map<?, ?> profile = kakaoAccount != null ? (Map<?, ?>) kakaoAccount.get("profile") : null;

            // ?대찓?쇱? 鍮꾩쫰?덉뒪 ???ъ궗 ?놁씠??誘몃룞???곹깭?????덉쑝誘濡??좏깮 ?뺣낫濡?泥섎━
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            if (email != null && email.isEmpty()) {
                email = null;
            }

            String nickname = profile != null ? (String) profile.get("nickname") : "?ъ슜??;
            String profileImageUrl = profile != null ? (String) profile.get("profile_image_url") : null;

            return new KakaoUserInfo(kakaoId, email, nickname, profileImageUrl);

        } catch (CustomException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.error("移댁뭅???ъ슜???뺣낫 議고쉶 ?ㅽ뙣: status={}", e.getStatusCode());
            throw CustomException.unauthorized("移댁뭅???ъ슜???뺣낫 議고쉶???ㅽ뙣?덉뒿?덈떎.");
        }
    }

    /**
     * 移댁뭅???ъ슜???뺣낫 ????덉퐫??     */
    public record KakaoUserInfo(
            String kakaoId,
            String email,
            String nickname,
            String profileImageUrl
    ) {}
}
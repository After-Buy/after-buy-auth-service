package com.After_Buy.AuthService.controller;

import com.After_Buy.AuthService.dto.response.UserStatsResponse;
import com.After_Buy.AuthService.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MSA ?대? ?듭떊 ?꾩슜 Auth Service 而⑦듃濡ㅻ윭
 * Admin Service ??Auth Service ?몄텧 寃쎈줈
 *
 * ?묎렐 ?쒗븳: InternalSecretFilter?먯꽌 X-Internal-Secret ?ㅻ뜑 寃利? * Base URL: /internal
 */
@Tag(name = "Internal Auth", description = "MSA ?대? ?듭떊 ?꾩슜 Auth API (Admin ??Auth)")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    // Repository瑜?吏곸젒 二쇱엯?섏? ?딄퀬 Service 怨꾩링???듯빐 ?묎렐 (?덉씠??援ъ“ 以??
    private final UserProfileService userProfileService;

    /**
     * GET /internal/users/stats
     * ?ъ슜???듦퀎 諛섑솚 (?꾩껜/?좉퇋 媛?낆옄 ??
     * Admin Service??/api/admin/users/stats 諛?/api/admin/dashboard ?대? ?몄텧
     *
     * @param days 議고쉶 湲곗? ?쇱닔 (湲곕낯: 7)
     * @return ?꾩껜 ?ъ슜???? ?꾩옱 湲곌컙 ?좉퇋 媛?낆옄, ?댁쟾 湲곌컙 ?좉퇋 媛?낆옄
     */
    @Operation(summary = "[Internal] ?ъ슜???듦퀎",
               description = "?꾩껜 ?ъ슜????諛?湲곌컙蹂??좉퇋 媛?낆옄 ?섎? 諛섑솚?⑸땲?? (Admin Service ?대? ?몄텧 ?꾩슜)")
    @Parameter(name = "X-Internal-Secret", description = "?대? 蹂댁븞 ??, required = true, in = ParameterIn.HEADER, schema = @Schema(type = "string"))
    @GetMapping("/users/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(userProfileService.getUserStats(days));
    }
}
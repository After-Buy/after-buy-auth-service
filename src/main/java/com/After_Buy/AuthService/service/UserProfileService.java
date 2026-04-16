package com.After_Buy.AuthService.service;

import com.After_Buy.AuthService.client.InternalNotificationClient;
import com.After_Buy.AuthService.dto.request.UpdateProfileRequest;
import com.After_Buy.AuthService.dto.response.UserProfileResponse;
import com.After_Buy.AuthService.dto.response.UserStatsResponse;
import com.After_Buy.AuthService.entity.User;
import com.After_Buy.AuthService.exception.CustomException;
import com.After_Buy.AuthService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 회원 신상 정보 및 앱 설정 패턴 환경 관리 서비스
 * 개인 맞춤형 프로필 개방 및 수정, 푸시 토글 상태 변경 그리고 총가입자 통계 등 유저의 부수적인 정보 통신처리를 주도합니다.
 *
 * @since : 2026.04.04
 * @version : 1.0.0
 * @author : 신태훈
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final InternalNotificationClient notificationClient;

    /**
     * 개별 로그인 회원의 프로필 데이터 추출/조회 규격
     * 토큰 검문망 필터를 무사 통과해 유입 요청을 한 회원의 개인적인 본인 열람을 허가하고 가공된 모델값을 만들어줍니다.
     *
     * @param userId : 액세스 층에서 소명되어 건네진 대상 계정 검색용 식별 PK 값
     * @return : 내부 유저 엔티티로부터 매핑 처리되어 API 규격에 맞춰진 프로필 제공용 반환 모델 객체
     */
    @Transactional(readOnly=true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = this.findUserById(userId);
        return UserProfileResponse.from(user);
    }

    /**
     * 회원의 표시 속성(닉네임, 외형 사진) 수동 수정 제어 로직
     * 닉네임의 문자 허용 길이를 모니터링 체킹하고 이미지 주소의 포맷을 점검해서 변경 요청을 레코드 데이터베이스에 머지 반영시킵니다.
     *
     * @param userId : 수정을 시도하는 접속 인증 유저 계정 데이터베이스 식별번호
     * @param request : 변경하고자 하는 요구 정보가 실려 들어온 컨트롤러 1차 매개체
     * @return : 모든 수정 연산이 끝난 뒤의 바뀐 내역이 병합 적용된 최신 프로필 제공 반환 모델
     * @throws CustomException : 닉네임 제한치수(50자 초과) 등 도메인 제네릭 허용 길이를 위반하였을 때 BAD REQUEST 배출
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = this.findUserById(userId);
        if (request.getNickname() != null) {
            if (request.getNickname().length() > 50) {
                throw CustomException.badRequest("INVALID_NICKNAME", "닉네임은 50자 이하여야 합니다.");
            }
            user.setNickname(request.getNickname());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(StringUtils.hasText(request.getProfileImageUrl()) ? request.getProfileImageUrl() : null);
        }
        log.info("프로필 수정 완료: userId={}", userId);
        return UserProfileResponse.from(user);
    }

    /**
     * 시스템 알림 송수신 상태 및 알람 중앙망 동기화 개입 로직
     * 프로필 옵션 화면에서 알림 여부를 껐다 켤 경우 DB의 수신 동의 상태를 변화시킬 뿐만 아니라,
     * Notification 전용 서비스에 이 사실을 즉시 반영 업데이트하도록 타 서버 통신을 단행합니다.
     *
     * @param userId : 설정을 토글 제어한 대상 사용자의 백엔드 식별 고유키
     * @param pushEnabled : 1(설정 ON) 또는 0(설정 OFF)으로 입력받는 알람 이진 상태 플래그 상수
     * @return : 오류 없이 성공적으로 반영이 완료되었음을 알려주는 알림 설정 활성화 최종 반환 리턴값
     */
    @Transactional
    public int updatePushEnabled(Long userId, int pushEnabled) {
        User user = this.findUserById(userId);
        user.setPushEnabled(pushEnabled);
        this.notificationClient.syncPushSettings(userId, pushEnabled);
        log.info("푸시 알림 설정 변경 완료: userId={}, pushEnabled={}", userId, pushEnabled);
        return pushEnabled;
    }

    /**
     * 서비스 내부 코드 전반에서 사용할 용도인 ID기반 유저 실존 엔티티 탐색 헬퍼 함수
     * 삭제된 사용자인지 아니면 존재하는지 등을 DB에 조회하여 재사용 목적 및 객체 조회 용도로 사용되는 리포지토리 보호 계층입니다.
     *
     * @param userId : 검색 목표물인 회원의 데이터베이스 ID
     * @return : 시스템에 보존되어 남아있는 실존 유효 유저 엔티티 모델 (행 단위)
     * @throws CustomException : 유저가 DB상 존재하지 않아서 조회가 거부될 때 배출되는 클라이언트 오탐지 에러
     */
    private User findUserById(Long userId) {
        return this.userRepository.findById(userId).orElseThrow(() -> CustomException.notFound("존재하지 않는 사용자입니다."));
    }

    /**
     * 운영 어드민 지표나 통계 데이터 반환을 책임지는 감시자용 집계 처리 로직
     * 외부 MSA 아키텍처 환경 속 Admin 서비스 등이 시스템 동향 및 유저 증감 상태를 측정할 시 DB에 분석 쿼리 조건을 위임하여 집계합니다.
     *
     * @param days : 과거 기준 몇 일(Day)만큼의 정보를 쪼개어 통계로 자를 것인지 지시하는 집계 범위값
     * @return : 증감률 비교 등 신규/가입 추이가 요약 비교 가능한 통합 데이터 전송용 Response 수치 모델
     */
    @Transactional(readOnly=true)
    public UserStatsResponse getUserStats(int days) {
        long totalUsers = this.userRepository.count();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentPeriodStart = now.minusDays(days);
        LocalDateTime prevPeriodStart = now.minusDays((long)days * 2L);
        
        long totalUsersPrev = this.userRepository.countByCreatedAtBefore(currentPeriodStart);
        long newUsersCurrent = this.userRepository.countByCreatedAtBetween(currentPeriodStart, now);
        long newUsersPrev = this.userRepository.countByCreatedAtBetween(prevPeriodStart, currentPeriodStart);
        
        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalUsersPrev7d(totalUsersPrev)
                .newUsersCurrentPeriod(newUsersCurrent)
                .newUsersPrevPeriod(newUsersPrev)
                .build();
    }
}

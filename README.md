# After-Buy Auth Service

전자기기 보증기간 관리 앱 **After-Buy**의 인증/회원 도메인을 담당하는 Spring Boot 기반 마이크로서비스입니다.  
카카오 소셜 로그인, JWT 인증, Refresh Token 관리, 사용자 프로필, 푸시 수신 동의 상태, 회원 탈퇴 시 타 서비스 데이터 정리 흐름을 담당합니다.

## 담당 범위

- 카카오 OAuth 인가 코드 기반 로그인 및 신규 사용자 자동 가입
- Access Token / Refresh Token 발급, 재발급, 로그아웃 처리
- JWT 기반 사용자 인증 필터 및 Security 설정
- 사용자 프로필 조회/수정 및 S3 Pre-signed URL 기반 프로필 이미지 업로드 지원
- Auth DB의 `push_enabled` 변경과 Notification Service의 `push_settings` 동기화
- 회원 탈퇴 시 Device / Notification Service 내부 API 호출을 통한 사용자 연관 데이터 삭제
- Admin Service에서 사용할 사용자 통계 내부 API 제공
- Swagger 기반 API 문서화 및 공통 응답/예외 처리 구조 설계

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.12 |
| Security | Spring Security, JWT(JJWT) |
| Database | MySQL, Spring Data JPA |
| External API | Kakao OAuth API, AWS S3 |
| Internal Communication | Spring WebFlux WebClient |
| Docs | Springdoc OpenAPI / Swagger UI |
| Test | JUnit 5, Spring Boot Test, MockWebServer, H2 |

## 핵심 구현

### 1. 카카오 OAuth 로그인과 자동 가입

클라이언트가 전달한 카카오 인가 코드를 Kakao API에 전달해 Access Token과 사용자 정보를 조회합니다.  
`kakao_provider_id`를 기준으로 기존 회원 여부를 판단하고, 신규 회원이면 `users` 테이블에 저장한 뒤 JWT 토큰 쌍을 발급합니다.

신규 가입 이후에는 Notification Service에 `push_settings` 초기 생성을 요청합니다. 이 호출은 회원가입 흐름을 막지 않도록 비동기 Fire-and-Forget 방식으로 처리했습니다.

### 2. JWT + Refresh Token 기반 인증

Access Token은 API 인증에 사용하고, Refresh Token은 DB에 저장해 재발급과 로그아웃을 제어합니다.

- Access Token 만료 시 `POST /api/auth/token/refresh`로 토큰 재발급
- Refresh Token 재발급 시 기존 Refresh Token을 `revoked=true`로 폐기
- 로그아웃 시 현재 기기의 Refresh Token만 폐기
- JWT 필터에서 토큰을 검증한 뒤 `UserPrincipal`로 사용자 ID 주입

### 3. MSA 간 사용자 상태 동기화

Auth Service는 사용자 생명주기의 기준 서비스입니다.  
회원가입, 푸시 수신 동의 변경, 회원 탈퇴 같은 이벤트가 발생하면 내부 API를 통해 다른 서비스와 상태를 맞춥니다.

| 이벤트 | 연동 대상 | 처리 방식 |
| --- | --- | --- |
| 신규 가입 | Notification Service | `POST /internal/push-settings/init` 비동기 호출 |
| 푸시 ON/OFF 변경 | Notification Service | `POST /internal/push-settings/sync` 동기 호출 |
| 회원 탈퇴 | Device Service | `DELETE /internal/devices/users/{userId}` 호출 |
| 회원 탈퇴 | Notification Service | `DELETE /internal/notifications/users/{userId}` 비동기 호출 |

내부 통신은 `X-Internal-Secret` 헤더를 사용해 외부 요청과 분리했습니다.

### 4. 프로필 이미지 업로드 분리

프로필 이미지는 Auth 도메인 데이터이므로 Auth Service에서 S3 Pre-signed URL을 발급합니다.  
클라이언트는 발급받은 URL로 S3에 직접 업로드하고, 최종 이미지 URL을 프로필 수정 API에 전달합니다.

## 주요 API

### Authentication

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/auth/kakao/login` | 카카오 로그인 및 자동 회원가입 |
| POST | `/api/auth/token/refresh` | Access / Refresh Token 재발급 |
| DELETE | `/api/auth/logout` | 현재 Refresh Token 폐기 |
| DELETE | `/api/auth/withdraw` | 회원 탈퇴 및 연관 데이터 삭제 |

### User Profile

| Method | Endpoint | 설명 |
| --- | --- | --- |
| GET | `/api/auth/users/me` | 내 프로필 조회 |
| PATCH | `/api/auth/users/me` | 닉네임 / 프로필 이미지 수정 |
| PATCH | `/api/auth/users/me/push` | 푸시 알림 수신 동의 변경 |
| POST | `/api/auth/images/presigned-url` | 프로필 이미지 업로드용 S3 Pre-signed URL 발급 |

### Internal

| Method | Endpoint | 설명 |
| --- | --- | --- |
| GET | `/internal/users/stats` | 관리자 대시보드용 사용자 통계 조회 |

## 프로젝트 구조

```text
src/main/java/com/After_Buy/AuthService/
├── client      # Kakao API, Device/Notification/Admin 내부 API WebClient
├── config      # Security, Swagger, S3, WebClient 설정
├── controller  # 인증, 프로필, 이미지, 내부 API 엔드포인트
├── dto         # Request / Response DTO
├── entity      # User, RefreshToken JPA Entity
├── exception   # CustomException, GlobalExceptionHandler
├── repository  # Spring Data JPA Repository
├── security    # JWT Provider, Authentication Filter, UserPrincipal
└── service     # 인증, 프로필, S3, 통계 비즈니스 로직
```

## 실행 방법

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하거나 IDE 실행 환경변수로 주입합니다.

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=auth_db
DB_USERNAME=root
DB_PASSWORD=password

KAKAO_CLIENT_ID=...
KAKAO_CLIENT_SECRET=...

JWT_SECRET=...
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=1209600000

INTERNAL_SECRET_KEY=...

SERVICES_DEVICE_URL=http://localhost:8082
SERVICES_NOTIFICATION_URL=http://localhost:8083
SERVICES_ADMIN_URL=http://localhost:8084

AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=...
```

`DB_NAME`에 지정한 MySQL 데이터베이스는 서버 실행 전에 미리 생성되어 있어야 합니다.

### 2. 빌드

```bash
./gradlew clean build
```

Windows 환경:

```bash
gradlew.bat clean build
```

### 3. 실행

```bash
./gradlew bootRun
```

기본 포트는 `8081`입니다.

### 4. Swagger

```text
http://localhost:8081/api/auth/swagger-ui.html
```

## 설계 포인트

- 사용자 인증과 사용자 생명주기를 Auth Service에 집중시켜 다른 서비스가 사용자 상태를 직접 판단하지 않도록 구성했습니다.
- Refresh Token을 DB에 저장하고 폐기 상태를 관리해 기기별 로그아웃과 토큰 재발급 제어가 가능하도록 했습니다.
- 신규 가입 시 Notification 초기화 실패가 회원가입 실패로 번지지 않도록 비동기 호출로 분리했습니다.
- 푸시 수신 동의 변경은 Auth DB와 Notification DB가 함께 맞아야 하므로 동기 호출로 처리했습니다.
- 서비스 간 직접 FK를 두지 않고 내부 API로 데이터를 정리해 MSA 경계를 유지했습니다.

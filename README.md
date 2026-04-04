# After-Buy Auth Service (인증 서비스)

## 📌 프로젝트 소개
MSA 기반의 After-Buy 플랫폼 환경에서 사용자 인증(카카오 소셜 로그인)과 보안(JWT), 회원가입 흐름 및 세션 연장을 중앙 설계하고 통제하는 기반 마이크로서비스입니다.

## 🛠️ 기술 스택
- **Language**: Java 17
- **Framework**: Spring Boot 3.x, Spring Security
- **Database**: MySQL, Spring Data JPA
- **Communication**: WebClient (Kakao API 및 타 MSA 내부 통신용)
- **Security**: JWT (Json Web Token)

## ✨ 주요 기능
- **소셜 로그인 (Kakao)**: 카카오 OAuth 2.0 인가 코드로 회원 정보를 파싱하여 서비스 자동 가입 및 로그인을 처리합니다.
- **JWT 토큰 인증**: Access Token 및 Refresh Token 발급/갱신 시스템으로 안전한 Stateless 인증 상태를 유지합니다.
- **회원 프로필 & 통계 관리**: 타 마이크로서비스(Admin 등)를 위한 회원 통계 및 기본 프로필 데이터를 관리합니다.
- **MSA 상호 간의 Sync (내부 통신)**: 회원가입, 탈퇴 등 유저 생명주기 발생 시 Notification(알림) 서비스와 Device(기기) 서비스의 데이터를 동기화하는 내부 브릿지를 제공합니다.
- **Swagger API 자동화**: `@Operation`, `@Tag` 등의 어노테이션 규격을 활용하여 프론트엔드 및 타 개발자를 위한 API 명세서를 자동으로 제공합니다.

## 📁 폴더 구조
```text
src/main/java/com/After_Buy/AuthService/
├── client      # 외부(Kakao) API 및 MSA 타 서비스 연동을 위한 WebClient 어댑터
├── config      # Security, Swagger, WebClient 인프라 설정 빈 등록 클래스
├── controller  # 사용자 인증 및 MSA 내부망 연동 REST API 엔드포인트
├── dto         # 계층 간 데이터 교환을 위한 Request / Response 모델
├── entity      # User, RefreshToken 등 데이터베이스 영속성 객체
├── exception   # 서비스 전역 에러 캐치 및 통합형 예외 응답 핸들러
├── repository  # 데이터베이스 접근을 담당하는 Spring Data JPA 계층
├── security    # JWT 토큰 생성, 파싱 및 시큐리티 권한 필터
└── service     # 토큰 제어, 회원가입, 통계 조회 등 핵심 비즈니스 로직
```

## 🚀 Getting Started (서버 실행 방법)

### 1. 환경 변수 세팅
앱 구동 전 필수적으로 주입되어야 하는 환경변수 리스트입니다. 프로젝트 최상단 디렉터리(루트 경로)에 `.env` 파일을 직접 생성한 뒤 아래 변수들을 기입하여 사용하거나, IDE 환경변수로 주입해 주세요.
* `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (MySQL 연동)
  > ⚠️ **데이터베이스 필수 조건**: 서버를 구동하기 전, MySQL 내부에 `DB_NAME`으로 지정할 이름(예: `auth_db`)의 빈 데이터베이스가 반드시 미리 생성되어 있어야 합니다.
* `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET` (카카오 개발자 앱 연동 ID 및 Secret)
* `JWT_SECRET` (256비트 이상의 시크릿 키 문자열)
* `INTERNAL_SECRET_KEY` (타 MSA와의 승인된 통신을 위한 내부 고유 키)
* `SERVICES_DEVICE_URL`, `SERVICES_NOTIFICATION_URL` (대상 MSA 주소)

### 2. 프로젝트 빌드
터미널을 열고 프로젝트 루트 경로에서 아래 명렁어를 통해 테스트를 제외한 쾌속 클린 빌드를 수행합니다.
```bash
# mac/linux의 경우 권한 처리 필요 (chmod +x gradlew)
./gradlew clean build -x test
```

### 3. 로컬 서버 실행
기본적인 `dev` 프로필을 활성화하여 내장 톰캣으로(기본 설정 포트: 8081) 서버를 가동합니다.
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. Swagger UI 활용 및 API 테스트
서버가 정상적으로 구동되었다면, 웹 브라우저에서 아래 주소로 접속하여 API 명세를 확인하고 즉각적인 테스트를 진행할 수 있습니다.
```text
http://localhost:8081/swagger-ui/index.html
```
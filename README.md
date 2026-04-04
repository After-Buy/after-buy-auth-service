# After-Buy Auth Service

After-Buy MSA ?꾨줈?앺듃??**?몄쬆/?멸? 留덉씠?щ줈?쒕퉬??*?낅땲??  
移댁뭅??OAuth 濡쒓렇?? JWT ?좏겙 諛쒓툒쨌媛깆떊, ?대? ?쒕퉬??媛??몄쬆???대떦?⑸땲??

---

## ?뱥 湲곗닠 ?ㅽ깮

| ??ぉ | ?댁슜 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Database | MySQL (AWS RDS) |
| Auth | JWT (jjwt 0.12.5) |
| OAuth | Kakao OAuth 2.0 |
| Docs | Swagger UI (Springdoc 2.6.0) |
| Container | Docker |

---

## ?? 濡쒖뺄 ?쒕쾭 ?ㅽ뻾 諛⑸쾿

### ?ъ쟾 ?붽뎄?ы빆

- Java 21 ?댁긽
- MySQL 8.0 ?댁긽 (?먮뒗 AWS RDS ?묒냽 ?뺣낫)
- 移댁뭅??媛쒕컻?????깅줉 諛?REST API Key

---

### 1?④퀎 ???섍꼍蹂???뚯씪 ?ㅼ젙

`.env_example` ?뚯씪??蹂듭궗?섏뿬 `.env` ?뚯씪???앹꽦?섍퀬 ?ㅼ젣 媛믪쑝濡?梨꾩썙二쇱꽭??

```bash
cp .env_example .env
```

`.env` ?뚯씪 ??ぉ ?ㅻ챸:

```env
# Database (AWS RDS ?먮뒗 濡쒖뺄 MySQL)
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=auth_db
DB_USERNAME=root
DB_PASSWORD=your_db_password

# JWT ?쒕챸 ??(理쒖냼 32諛붿씠???댁긽 ?쒕뜡 臾몄옄??沅뚯옣)
JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_EXPIRATION=3600000       # ?≪꽭???좏겙 留뚮즺 (ms) - 湲곕낯 1?쒓컙
JWT_REFRESH_EXPIRATION=1209600000  # 由ы봽?덉떆 ?좏겙 留뚮즺 (ms) - 湲곕낯 14??
# 移댁뭅??OAuth
KAKAO_CLIENT_ID=your_kakao_rest_api_key
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_REDIRECT_URI=http://localhost:8081/login/oauth2/code/kakao

# MSA ?대? ?듭떊 怨듭쑀 鍮꾨???INTERNAL_SECRET_KEY=your_internal_secret_key

# AWS S3 (?꾨줈???대?吏 Pre-signed URL, ?ъ슜 ???쒖꽦??
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=your_s3_bucket_name
```

---

### 2?④퀎 ???곗씠?곕쿋?댁뒪 以鍮?
MySQL???묒냽?섏뿬 ?곗씠?곕쿋?댁뒪瑜??앹꽦?⑸땲??

```sql
CREATE DATABASE auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> ?뚯씠釉붿? ?쒕쾭 ?ㅽ뻾 ??JPA `ddl-auto`???섑빐 ?먮룞 ?앹꽦?⑸땲??

---

### 3?④퀎 ???쒕쾭 ?ㅽ뻾

#### Gradle濡?吏곸젒 ?ㅽ뻾 (沅뚯옣)

```bash
# Windows
./gradlew.bat bootRun

# Mac / Linux
./gradlew bootRun
```

#### JAR 鍮뚮뱶 ???ㅽ뻾

```bash
# 鍮뚮뱶 (?뚯뒪???쒖쇅)
./gradlew bootJar -x test

# ?ㅽ뻾
java -jar build/libs/AuthService-0.0.1-SNAPSHOT.jar
```

?쒕쾭媛 ?뺤긽 湲곕룞?섎㈃ ?꾨옒 ?ы듃?먯꽌 ?묐떟?⑸땲??

```
http://localhost:8081
```

---

### 4?④퀎 ??API 臾몄꽌 ?뺤씤 (Swagger UI)

?쒕쾭 ?ㅽ뻾 ??釉뚮씪?곗??먯꽌 ?묒냽:

```
http://localhost:8081/swagger-ui/index.html
```

---

## ?맫 Docker濡??ㅽ뻾

```bash
# ?대?吏 鍮뚮뱶
docker build -t after-buy-auth-service .

# 而⑦뀒?대꼫 ?ㅽ뻾 (.env ?뚯씪 二쇱엯)
docker run -d \
  --name auth-service \
  --env-file .env \
  -p 8081:8081 \
  after-buy-auth-service
```

---

## ?뱻 二쇱슂 API ?붾뱶?ъ씤??
| Method | URL | ?ㅻ챸 |
|---|---|---|
| GET | `/auth/kakao/login` | 移댁뭅??OAuth 濡쒓렇??由щ떎?대젆??|
| GET | `/auth/kakao/callback` | 移댁뭅??OAuth 肄쒕갚 泥섎━ |
| POST | `/auth/refresh` | Access Token ?щ컻湲?|
| DELETE | `/auth/logout` | 濡쒓렇?꾩썐 |
| GET | `/internal/auth/validate` | ?대? ?쒕퉬?ㅼ슜 ?좏겙 寃利?|

> ?꾩껜 API 紐낆꽭??Swagger UI瑜?李멸퀬?섏꽭??

---

## ?뵍 蹂댁븞 二쇱쓽?ы빆

- `.env` ?뚯씪? **?덈? Git??而ㅻ컠?섏? 留덉꽭??* (`.gitignore`???깅줉??
- `JWT_SECRET`? 理쒖냼 32諛붿씠???댁긽???쒕뜡 媛믪쓣 ?ъ슜?섏꽭??- `INTERNAL_SECRET_KEY`??MSA ??紐⑤뱺 ?쒕퉬?ㅼ? ?숈씪??媛믪쓣 怨듭쑀?댁빞 ?⑸땲??
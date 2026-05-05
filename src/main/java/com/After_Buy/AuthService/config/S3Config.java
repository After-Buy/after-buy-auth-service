package com.After_Buy.AuthService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 Pre-signed URL 발급을 위한 설정
 * Spring Cloud AWS가 자동 구성하지 않는 S3Presigner 빈을 수동으로 등록합니다.
 *
 * @since : 2026.05.05
 * @version : 1.0.0
 * @author : 신태훈
 */
@Configuration
public class S3Config {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * S3Presigner 빈 등록
     * Pre-signed URL 생성에 필요한 AWS 서명 클라이언트를 구성합니다.
     *
     * @return : AWS IAM 자격 증명이 적용된 S3Presigner 인스턴스
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }
}

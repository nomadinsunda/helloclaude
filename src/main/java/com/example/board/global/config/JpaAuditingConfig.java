package com.example.board.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 메인 클래스에 {@code @EnableJpaAuditing}을 두면 {@code @WebMvcTest}가 실패하므로 별도 설정으로 분리한다.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class JpaAuditingConfig {
}

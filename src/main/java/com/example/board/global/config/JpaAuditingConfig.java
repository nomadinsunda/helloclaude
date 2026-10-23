package com.example.board.global.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 메인 클래스에 {@code @EnableJpaAuditing}을 두면 {@code @WebMvcTest}가 실패하므로 별도 설정으로 분리한다.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    /**
     * PostgreSQL {@code TIMESTAMP(6)}은 마이크로초까지만 저장한다.
     * 나노초를 미리 잘라 두어 저장 직후 응답 값과 DB에서 다시 읽은 값이 같도록 한다.
     */
    @Bean
    DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }
}

package com.example.board;

import com.example.board.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

/**
 * docker-compose 없이 Testcontainers PostgreSQL로 앱을 띄운다 (IntelliJ에서 main 실행 또는 {@code ./gradlew bootTestRun}).
 */
public class TestBoardApplication {

    public static void main(String[] args) {
        SpringApplication.from(BoardApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}

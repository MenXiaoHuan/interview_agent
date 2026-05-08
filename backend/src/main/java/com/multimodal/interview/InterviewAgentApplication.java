package com.multimodal.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot 应用启动类。
 *
 * <p>启用异步执行与重试能力，作为后端服务的统一入口。</p>
 */
@EnableRetry
@SpringBootApplication
@EnableAsync
public class InterviewAgentApplication {

    /**
     * 启动应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentApplication.class, args);
        System.out.println("~~~（启动成功）~~~");
    }
}

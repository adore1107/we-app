package com.songjia.textile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宋家纺织B2B小程序后端应用启动类
 */
@SpringBootApplication
public class TextileApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextileApplication.class, args);
        System.out.println("=================================");
        System.out.println("🚀 宋家纺织B2B后端服务启动成功！");
        System.out.println("🌐 API服务地址: http://localhost:8080");
        System.out.println("📖 API文档: http://localhost:8080/api-docs");
        System.out.println("=================================");
    }
}



package com.songjia.textile;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123";
        String hashedPassword = "$2a$10$X5wFuQBxDq4wZJ8rQk7B5.LJzV9FZ3qHzG3yKp6rJ7NxJB5pYqGHW";

        System.out.println("原始密码: " + rawPassword);
        System.out.println("数据库中的哈希: " + hashedPassword);

        boolean matches = encoder.matches(rawPassword, hashedPassword);
        System.out.println("验证结果: " + matches);

        if (!matches) {
            System.out.println("\n密码不匹配！重新生成一个新的哈希：");
            String newHash = encoder.encode(rawPassword);
            System.out.println("新的BCrypt哈希: " + newHash);

            boolean newMatches = encoder.matches(rawPassword, newHash);
            System.out.println("新哈希验证结果: " + newMatches);
        }
    }
}

package com.songjia.textile;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";

        // 生成3个不同的哈希（BCrypt每次生成的哈希都不同，但都能验证）
        System.out.println("为密码 'admin123' 生成BCrypt哈希：\n");

        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(rawPassword);
            boolean matches = encoder.matches(rawPassword, hash);

            System.out.println("哈希 " + i + ":");
            System.out.println(hash);
            System.out.println("验证结果: " + matches);
            System.out.println();
        }

        System.out.println("\n请复制上面任意一个哈希值，用于更新数据库");
    }
}

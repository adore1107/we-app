package com.songjia.textile.service;

import com.songjia.textile.dto.AdminLoginRequest;
import com.songjia.textile.dto.AdminLoginResponse;
import com.songjia.textile.entity.Admin;
import com.songjia.textile.entity.AdminLog;
import com.songjia.textile.mapper.AdminLogMapper;
import com.songjia.textile.mapper.AdminMapper;
import com.songjia.textile.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 管理员服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final AdminMapper adminMapper;
    private final AdminLogMapper adminLogMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 管理员登录
     *
     * @param request 登录请求
     * @param ip      登录IP
     * @return 登录响应（包含Token）
     */
    public AdminLoginResponse login(AdminLoginRequest request, String ip) {
        log.info("管理员登录请求: username={}, ip={}", request.getUsername(), ip);

        // 1. 查找管理员
        Optional<Admin> adminOpt = adminMapper.findByUsernameAndStatus(request.getUsername());

        if (adminOpt.isEmpty()) {
            log.warn("登录失败: 用户不存在或已禁用 - username={}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }

        Admin admin = adminOpt.get();

        // 2. 验证密码
        log.debug("开始验证密码 - 输入密码: {}, 数据库哈希: {}", request.getPassword(), admin.getPassword());
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), admin.getPassword());
        log.debug("密码验证结果: {}", passwordMatch);

        if (!passwordMatch) {
            log.warn("登录失败: 密码错误 - username={}", request.getUsername());

            // 记录登录失败日志
            recordLoginLog(admin.getId(), admin.getUsername(), "login_failed",
                          "密码错误", ip);

            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 更新最后登录信息
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setLastLoginIp(ip);
        adminMapper.updateById(admin);

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(admin.getUsername(), admin.getId(), admin.getRole());

        // 5. 记录登录成功日志
        recordLoginLog(admin.getId(), admin.getUsername(), "login_success",
                      "登录成功", ip);

        log.info("管理员登录成功: username={}, role={}", admin.getUsername(), admin.getRole());

        // 6. 返回登录响应
        return AdminLoginResponse.builder()
                .token(token)
                .id(admin.getId())
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .role(admin.getRole())
                .build();
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Integer adminId, String username, String action,
                                String content, String ip) {
        AdminLog adminLog = AdminLog.builder()
                .adminId(adminId)
                .adminName(username)
                .module("auth")
                .action(action)
                .content(content)
                .ip(ip)
                .createdAt(LocalDateTime.now())
                .build();

        adminLogMapper.insert(adminLog);
    }

    /**
     * 验证Token
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * 从Token获取管理员信息
     */
    @Transactional(readOnly = true)
    public Admin getAdminFromToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return adminMapper.findByUsername(username).orElse(null);
    }

    /**
     * 记录操作日志
     */
    public void recordLog(Integer adminId, String adminName, String module,
                         String action, String content, String ip) {
        AdminLog adminLog = AdminLog.builder()
                .adminId(adminId)
                .adminName(adminName)
                .module(module)
                .action(action)
                .content(content)
                .ip(ip)
                .createdAt(LocalDateTime.now())
                .build();

        adminLogMapper.insert(adminLog);
        log.info("操作日志已记录: admin={}, module={}, action={}", adminName, module, action);
    }
}

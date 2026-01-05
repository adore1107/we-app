package com.songjia.textile.controller;

import com.songjia.textile.common.Result;
import com.songjia.textile.dto.AdminLoginRequest;
import com.songjia.textile.dto.AdminLoginResponse;
import com.songjia.textile.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 获取客户端IP
            String ip = getClientIp(httpRequest);

            AdminLoginResponse response = adminService.login(request, ip);

            return Result.success("登录成功", response);
        } catch (RuntimeException e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("登录异常", e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    /**
     * 验证Token（用于前端路由守卫）
     */
    @GetMapping("/verify")
    public Result<Boolean> verifyToken(@RequestHeader("Authorization") String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("Token格式错误");
            }

            String token = authorization.substring(7);
            boolean valid = adminService.validateToken(token);

            if (valid) {
                return Result.success("Token有效", true);
            } else {
                return Result.error("Token无效或已过期");
            }
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return Result.error("Token验证失败");
        }
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    public Result<AdminLoginResponse> getAdminInfo(@RequestHeader("Authorization") String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return Result.error("未授权");
            }

            String token = authorization.substring(7);
            var admin = adminService.getAdminFromToken(token);

            if (admin == null) {
                return Result.error("管理员不存在");
            }

            AdminLoginResponse response = AdminLoginResponse.builder()
                    .id(admin.getId())
                    .username(admin.getUsername())
                    .realName(admin.getRealName())
                    .role(admin.getRole())
                    .build();

            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return Result.error("获取管理员信息失败");
        }
    }

    /**
     * 登出（前端清除Token即可，这里可以记录日志）
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization,
                              HttpServletRequest httpRequest) {
        try {
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                var admin = adminService.getAdminFromToken(token);

                if (admin != null) {
                    String ip = getClientIp(httpRequest);
                    adminService.recordLog(admin.getId(), admin.getUsername(),
                            "auth", "logout", "退出登录", ip);
                    log.info("管理员退出登录: username={}", admin.getUsername());
                }
            }

            return Result.success("退出成功", null);
        } catch (Exception e) {
            log.error("退出登录异常", e);
            return Result.success("退出成功", null);
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

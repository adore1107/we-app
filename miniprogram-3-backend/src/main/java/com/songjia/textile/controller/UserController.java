package com.songjia.textile.controller;

import com.songjia.textile.common.Result;
import com.songjia.textile.entity.User;
import com.songjia.textile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 微信登录（支持完整用户信息）
     */
    @PostMapping("/login")
    public Result<User> login(@RequestParam @NotBlank String openid,
                               @RequestParam(required = false) String nickname,
                               @RequestParam(required = false) String avatarUrl,
                               @RequestParam(required = false, name = "company_name") String companyName,
                               @RequestParam(required = false, name = "real_name") String realName,
                               @RequestParam(required = false) String position,
                               @RequestParam(required = false) String industry,
                               @RequestParam(required = false) String city,
                               @RequestParam(required = false) String phone) {
        try {
            log.info("用户登录请求: openid={}, nickname={}, companyName={}", openid, nickname, companyName);

            // 支持完整用户信息的登录
            User user = userService.createOrUpdateUserWithFullInfo(
                openid, nickname, avatarUrl,
                companyName, realName, position, industry, city, phone
            );

            log.info("登录成功: userId={}, openid={}", user.getId(), openid);
            return Result.success("登录成功", user);
        } catch (Exception e) {
            log.error("登录失败: openid={}", openid, e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile/{userId}")
    public Result<User> getUserProfile(@PathVariable @NotNull Integer userId) {
        try {
            return userService.getUserById(userId)
                    .map(user -> Result.success("获取成功", user))
                    .orElse(Result.error("用户不存在"));
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}", userId, e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile/{userId}")
    public Result<User> updateUserProfile(@PathVariable @NotNull Integer userId,
                                          @RequestParam(required = false) String phone,
                                          @RequestParam(required = false) String companyName,
                                          @RequestParam(required = false) String realName,
                                          @RequestParam(required = false) String position,
                                          @RequestParam(required = false) String industry,
                                          @RequestParam(required = false) String city) {
        try {
            User user = userService.updateUserProfile(userId, phone, companyName, realName, position, industry, city);
            return Result.success("更新成功", user);
        } catch (Exception e) {
            log.error("更新用户信息失败: userId={}", userId, e);
            return Result.error("更新失败");
        }
    }

    /**
     * 检查用户是否存在
     */
    @GetMapping("/exists/{userId}")
    public Result<Boolean> checkUserExists(@PathVariable @NotNull Integer userId) {
        try {
            boolean exists = userService.userExists(userId);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查用户存在失败: userId={}", userId, e);
            return Result.error("检查失败");
        }
    }
}
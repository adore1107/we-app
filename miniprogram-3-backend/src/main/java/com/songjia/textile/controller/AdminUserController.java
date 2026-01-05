package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.User;
import com.songjia.textile.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 * 提供用户的查询、统计等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String openid) {
        try {
            Page<User> pageable = new Page<>(page + 1, size);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            // 筛选条件
            if (openid != null && !openid.trim().isEmpty()) {
                queryWrapper.like("openid", openid);
            }

            // 按创建时间降序排列
            queryWrapper.orderByDesc("created_at", "id");

            IPage<User> users = userMapper.selectPage(pageable, queryWrapper);

            log.info("管理员获取用户列表: openid={}, total={}", openid, users.getTotal());

            return Result.success("获取成功", users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.error("获取用户列表失败");
        }
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/statistics")
    public Result<Object> getUserStatistics() {
        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            long totalCount = userMapper.selectCount(queryWrapper);

            log.info("获取用户统计: 总数={}", totalCount);

            return Result.success("获取成功", new Object() {
                public final long total = totalCount;
            });
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return Result.error("获取用户统计失败");
        }
    }
}

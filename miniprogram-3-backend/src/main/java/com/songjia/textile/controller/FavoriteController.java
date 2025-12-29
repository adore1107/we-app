package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Favorite;
import com.songjia.textile.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<Favorite> addFavorite(@RequestParam @NotNull Integer userId,
                                         @RequestParam @NotNull Integer productId) {
        try {
            Favorite favorite = favoriteService.addFavorite(userId, productId);
            return Result.success("收藏成功", favorite);
        } catch (RuntimeException e) {
            log.error("添加收藏失败: userId={}, productId={}", userId, productId, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加收藏失败: userId={}, productId={}", userId, productId, e);
            return Result.error("收藏失败");
        }
    }

    /**
     * 取消收藏（支持POST和DELETE）
     */
    @DeleteMapping("/remove")
    public Result<String> removeFavorite(@RequestParam @NotNull Integer userId,
                                           @RequestParam @NotNull Integer productId) {
        try {
            favoriteService.removeFavorite(userId, productId);
            return Result.success("取消收藏成功");
        } catch (RuntimeException e) {
            log.error("取消收藏失败: userId={}, productId={}", userId, productId, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("取消收藏失败: userId={}, productId={}", userId, productId, e);
            return Result.error("取消收藏失败");
        }
    }

    /**
     * 取消收藏（POST版本，用于微信小程序）
     */
    @PostMapping("/remove")
    public Result<String> removeFavoritePost(@RequestParam @NotNull Integer userId,
                                             @RequestParam @NotNull Integer productId) {
        return removeFavorite(userId, productId);
    }

    /**
     * 切换收藏状态（添加/取消）
     */
    @PostMapping("/toggle")
    public Result<Favorite> toggleFavorite(@RequestParam @NotNull Integer userId,
                                           @RequestParam @NotNull Integer productId) {
        try {
            Favorite favorite = favoriteService.toggleFavorite(userId, productId);
            if (favorite != null) {
                return Result.success("收藏成功", favorite);
            } else {
                // 返回null值，让前端知道是取消收藏操作
                return Result.<Favorite>success("取消收藏成功", null);
            }
        } catch (RuntimeException e) {
            log.error("切换收藏失败: userId={}, productId={}", userId, productId, e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("切换收藏失败: userId={}, productId={}", userId, productId, e);
            return Result.error("操作失败");
        }
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/list/{userId}")
    public Result<IPage<Favorite>> getUserFavorites(
            @PathVariable @NotNull Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Favorite> favorites = favoriteService.getUserFavorites(userId, page, size);
            return Result.success("获取成功", favorites);
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: userId={}", userId, e);
            return Result.error("获取收藏列表失败");
        }
    }

    /**
     * 检查用户是否已收藏某商品
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam @NotNull Integer userId,
                                         @RequestParam @NotNull Integer productId) {
        try {
            boolean isFavorited = favoriteService.isFavorited(userId, productId);
            return Result.success("检查成功", isFavorited);
        } catch (Exception e) {
            log.error("检查收藏状态失败: userId={}, productId={}", userId, productId, e);
            return Result.error("检查失败");
        }
    }

    /**
     * 获取用户收藏数量
     */
    @GetMapping("/count/{userId}")
    public Result<Integer> getUserFavoriteCount(@PathVariable @NotNull Integer userId) {
        try {
            int count = favoriteService.getUserFavoriteCount(userId);
            return Result.success("获取成功", count);
        } catch (Exception e) {
            log.error("获取用户收藏数量失败: userId={}", userId, e);
            return Result.error("获取数量失败");
        }
    }

    /**
     * 清空用户收藏
     */
    @DeleteMapping("/clear/{userId}")
    public Result<String> clearUserFavorites(@PathVariable @NotNull Integer userId) {
        try {
            favoriteService.clearUserFavorites(userId);
            return Result.success("清空收藏成功");
        } catch (Exception e) {
            log.error("清空用户收藏失败: userId={}", userId, e);
            return Result.error("清空失败");
        }
    }
}
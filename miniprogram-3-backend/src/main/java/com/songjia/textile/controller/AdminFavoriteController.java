package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Favorite;
import com.songjia.textile.entity.Product;
import com.songjia.textile.mapper.FavoriteMapper;
import com.songjia.textile.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理员收藏管理控制器
 * 提供收藏的查询、删除等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/favorite")
@RequiredArgsConstructor
public class AdminFavoriteController {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    /**
     * 获取收藏列表（分页，包含商品详细信息）
     */
    @GetMapping("/list")
    public Result<IPage<Favorite>> getFavoriteList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer productId) {
        try {
            Page<Favorite> pageable = new Page<>(page + 1, size);
            QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();

            // 筛选条件
            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
            if (productId != null) {
                queryWrapper.eq("product_id", productId);
            }

            // 按创建时间降序排列
            queryWrapper.orderByDesc("created_at", "id");

            IPage<Favorite> favorites = favoriteMapper.selectPage(pageable, queryWrapper);

            // 填充商品详细信息
            for (Favorite favorite : favorites.getRecords()) {
                Product product = productMapper.selectById(favorite.getProductId());
                if (product != null) {
                    favorite.setProductName(product.getName());
                    favorite.setProductImage(product.getMainImage());
                    // 优先使用批发价，如果没有则使用零售价
                    favorite.setProductPrice(product.getWholesalePrice() != null ?
                        product.getWholesalePrice() : product.getRetailPrice());
                    favorite.setProductStock(product.getStock());
                    favorite.setProductStatus(product.getStatus());
                }
            }

            log.info("管理员获取收藏列表: userId={}, productId={}, total={}",
                    userId, productId, favorites.getTotal());

            return Result.success("获取成功", favorites);
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return Result.error("获取收藏列表失败");
        }
    }

    /**
     * 获取收藏详情
     */
    @GetMapping("/detail/{id}")
    public Result<Favorite> getFavoriteDetail(@PathVariable @NotNull Integer id) {
        try {
            Favorite favorite = favoriteMapper.selectById(id);
            if (favorite == null) {
                return Result.error("收藏不存在");
            }

            // 填充商品详细信息
            Product product = productMapper.selectById(favorite.getProductId());
            if (product != null) {
                favorite.setProductName(product.getName());
                favorite.setProductImage(product.getMainImage());
                // 优先使用批发价，如果没有则使用零售价
                favorite.setProductPrice(product.getWholesalePrice() != null ?
                    product.getWholesalePrice() : product.getRetailPrice());
                favorite.setProductStock(product.getStock());
                favorite.setProductStatus(product.getStatus());
            }

            log.info("管理员获取收藏详情: id={}", id);
            return Result.success("获取成功", favorite);
        } catch (Exception e) {
            log.error("获取收藏详情失败: id={}", id, e);
            return Result.error("获取收藏详情失败");
        }
    }

    /**
     * 删除收藏（软删除）
     */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<String> deleteFavorite(@PathVariable @NotNull Integer id) {
        try {
            Favorite favorite = favoriteMapper.selectById(id);
            if (favorite == null) {
                return Result.error("收藏不存在");
            }

            // 使用 MyBatis-Plus 的 deleteById 会自动软删除（因为有 @TableLogic 注解）
            int result = favoriteMapper.deleteById(id);

            if (result > 0) {
                log.info("删除收藏成功: id={}", id);
                return Result.success("删除成功", "OK");
            } else {
                log.error("删除收藏失败: id={}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除收藏异常: id={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除收藏（软删除）
     */
    @DeleteMapping("/batch-delete")
    @Transactional
    public Result<String> batchDeleteFavorites(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的收藏");
            }

            // 使用 MyBatis-Plus 的 deleteBatchIds 会自动软删除（因为有 @TableLogic 注解）
            int result = favoriteMapper.deleteBatchIds(ids);

            log.info("批量删除收藏: 请求删除{}个, 实际删除{}个", ids.size(), result);

            if (result > 0) {
                return Result.success("删除成功", "成功删除" + result + "个收藏");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除收藏异常: ids={}", ids, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 统计收藏数据
     */
    @GetMapping("/statistics")
    public Result<Object> getFavoriteStatistics() {
        try {
            QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
            long totalCount = favoriteMapper.selectCount(queryWrapper);

            log.info("获取收藏统计: 总数={}", totalCount);

            return Result.success("获取成功", new Object() {
                public final long total = totalCount;
            });
        } catch (Exception e) {
            log.error("获取收藏统计失败", e);
            return Result.error("获取收藏统计失败");
        }
    }
}

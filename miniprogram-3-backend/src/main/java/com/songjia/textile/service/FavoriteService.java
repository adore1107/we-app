package com.songjia.textile.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Favorite;
import com.songjia.textile.entity.Product;
import com.songjia.textile.mapper.FavoriteMapper;
import com.songjia.textile.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 收藏服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * 添加收藏（支持恢复软删除的记录）
     */
    public Favorite addFavorite(Integer userId, Integer productId) {
        // 先查询是否存在收藏记录（包括软删除的）
        Favorite existingFavorite = favoriteMapper.findByUserIdAndProductIdIncludeDeleted(userId, productId);

        if (existingFavorite != null) {
            // 如果记录存在且未删除，说明已经收藏
            if (existingFavorite.getDeleted() == 0) {
                log.warn("商品已在收藏中: userId={}, productId={}", userId, productId);
                throw new RuntimeException("商品已在收藏中");
            }

            // 如果记录存在但被软删除，使用自定义方法恢复（绕过 @TableLogic）
            log.info("恢复软删除的收藏记录: userId={}, productId={}, favoriteId={}",
                     userId, productId, existingFavorite.getId());
            int restoreResult = favoriteMapper.restoreFavorite(userId, productId);
            if (restoreResult <= 0) {
                log.error("恢复收藏记录失败: userId={}, productId={}", userId, productId);
                throw new RuntimeException("恢复收藏记录失败");
            }
            log.info("✅ 成功恢复收藏记录，影响行数: {}", restoreResult);

            // 更新商品收藏次数
            int favoriteCount = favoriteMapper.countByUserId(userId);
            productMapper.updateFavoriteCount(productId, favoriteCount);

            // 重新查询恢复后的记录（确保 deleted=0）
            Favorite restoredFavorite = favoriteMapper.findByUserIdAndProductId(userId, productId);
            log.info("✅ 用户恢复收藏成功: userId={}, productId={}, deleted={}",
                     userId, productId, restoredFavorite != null ? restoredFavorite.getDeleted() : "null");
            return restoredFavorite != null ? restoredFavorite : existingFavorite;
        }

        // 验证商品是否存在
        Optional<Product> product = productService.getProductById(productId);
        if (product.isEmpty() || !product.get().getStatus()) {
            throw new RuntimeException("商品不存在或已下架");
        }

        // 创建新的收藏记录
        Favorite favorite = Favorite.builder()
                .userId(userId)
                .productId(productId)
                .deleted(0)
                .build();

        int result = favoriteMapper.insert(favorite);
        if (result <= 0) {
            throw new RuntimeException("添加收藏失败");
        }

        // 更新商品收藏次数
        int favoriteCount = favoriteMapper.countByUserId(userId);
        productMapper.updateFavoriteCount(productId, favoriteCount);

        log.info("用户添加收藏: userId={}, productId={}", userId, productId);
        return favorite;
    }

    /**
     * 取消收藏
     */
    public void removeFavorite(Integer userId, Integer productId) {
        Favorite existingFavorite = favoriteMapper.findByUserIdAndProductId(userId, productId);
        if (existingFavorite == null) {
            throw new RuntimeException("收藏不存在");
        }

        int result = favoriteMapper.deleteByUserIdAndProductId(userId, productId);
        if (result <= 0) {
            throw new RuntimeException("取消收藏失败");
        }

        // 更新商品收藏次数
        int favoriteCount = favoriteMapper.countByUserId(userId);
        productMapper.updateFavoriteCount(productId, favoriteCount);

        log.info("用户取消收藏: userId={}, productId={}", userId, productId);
    }

    /**
     * 获取用户收藏列表
     */
    @Transactional(readOnly = true)
    public IPage<Favorite> getUserFavorites(Integer userId, int page, int size) {
        Page<Favorite> pageable = new Page<>(page + 1, size);
        IPage<Favorite> result = favoriteMapper.findByUserId(userId, pageable);
        log.info("获取用户收藏列表: userId={}, 第 {} 页, 每页 {} 个, 共 {} 条记录",
                userId, page, size, result.getTotal());
        return result;
    }

    /**
     * 获取用户收藏列表（非分页）
     */
    @Transactional(readOnly = true)
    public List<Favorite> getAllUserFavorites(Integer userId) {
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        log.info("获取用户所有收藏: userId={}, 数量={}", userId, favorites.size());
        return favorites;
    }

    /**
     * 检查用户是否已收藏某商品
     */
    @Transactional(readOnly = true)
    public boolean isFavorited(Integer userId, Integer productId) {
        Favorite favorite = favoriteMapper.findByUserIdAndProductId(userId, productId);
        return favorite != null;
    }

    /**
     * 获取用户收藏数量
     */
    @Transactional(readOnly = true)
    public int getUserFavoriteCount(Integer userId) {
        int count = favoriteMapper.countByUserId(userId);
        log.info("用户收藏数量: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 切换收藏状态（添加/取消）
     */
    public Favorite toggleFavorite(Integer userId, Integer productId) {
        if (isFavorited(userId, productId)) {
            removeFavorite(userId, productId);
            return null;
        } else {
            return addFavorite(userId, productId);
        }
    }

    /**
     * 清空用户所有收藏
     */
    public void clearUserFavorites(Integer userId) {
        // 直接删除用户的所有收藏记录
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        for (Favorite favorite : favorites) {
            favoriteMapper.deleteById(favorite.getId());
        }
        log.info("清空用户收藏: userId={}, 删除数量={}", userId, favorites.size());
    }

    /**
     * 根据ID获取收藏记录
     */
    @Transactional(readOnly = true)
    public Optional<Favorite> getFavoriteById(Integer id) {
        Favorite favorite = favoriteMapper.selectById(id);
        return Optional.ofNullable(favorite);
    }

    /**
     * 删除收藏记录（逻辑删除）
     */
    public boolean deleteFavorite(Integer id) {
        int result = favoriteMapper.deleteById(id);
        if (result > 0) {
            log.info("删除收藏记录: id={}", id);
            return true;
        } else {
            log.warn("删除收藏记录失败: id={}", id);
            return false;
        }
    }
}
package com.songjia.textile.service;

import com.songjia.textile.entity.ProductView;
import com.songjia.textile.mapper.ProductViewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 商品浏览记录服务类
 * 简单记录每次用户浏览，不做去重处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductViewService {

    private final ProductViewMapper productViewMapper;
    private final ProductService productService;

    /**
     * 记录商品浏览（进入页面时调用）
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 返回浏览记录ID，用于后续更新浏览时长
     */
    public Integer recordProductView(Integer userId, Integer productId) {
        log.info("记录商品浏览 - userId: {}, productId: {}", userId, productId);

        LocalDateTime now = LocalDateTime.now();

        // 新增浏览记录
        ProductView newView = ProductView.builder()
                .userId(userId)
                .productId(productId)
                .viewStartTime(now)
                .durationSeconds(0)
                .createdAt(now)
                .build();

        productViewMapper.insert(newView);

        // 增加商品总浏览次数
        productService.incrementViewCount(productId);

        log.info("浏览记录已添加 - viewId: {}, 开始时间: {}", newView.getId(), now);
        return newView.getId();
    }

    /**
     * 更新浏览时长（离开页面时调用）
     *
     * @param viewId 浏览记录ID
     * @param durationSeconds 浏览时长（秒）
     */
    public void updateViewDuration(Integer viewId, Integer durationSeconds) {
        log.info("更新浏览时长 - viewId: {}, duration: {}秒", viewId, durationSeconds);

        ProductView view = productViewMapper.selectById(viewId);
        if (view != null) {
            view.setViewEndTime(LocalDateTime.now());
            view.setDurationSeconds(durationSeconds);
            productViewMapper.updateById(view);

            log.info("浏览时长已更新 - viewId: {}, 总时长: {}秒", viewId, durationSeconds);
        } else {
            log.warn("浏览记录不存在 - viewId: {}", viewId);
        }
    }

    /**
     * 通过用户和商品更新最近一次浏览时长（备用方法）
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @param durationSeconds 浏览时长（秒）
     */
    public void updateLatestViewDuration(Integer userId, Integer productId, Integer durationSeconds) {
        log.info("更新最近浏览时长 - userId: {}, productId: {}, duration: {}秒",
                userId, productId, durationSeconds);

        // 查找最近的一条浏览记录
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProductView> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("product_id", productId);
        queryWrapper.orderByDesc("created_at");
        queryWrapper.last("LIMIT 1");

        ProductView view = productViewMapper.selectOne(queryWrapper);
        if (view != null) {
            view.setViewEndTime(LocalDateTime.now());
            view.setDurationSeconds(durationSeconds);
            productViewMapper.updateById(view);

            log.info("最近浏览时长已更新 - viewId: {}, 总时长: {}秒", view.getId(), durationSeconds);
        } else {
            log.warn("未找到浏览记录 - userId: {}, productId: {}", userId, productId);
        }
    }

    /**
     * 获取商品的总点击次数（PV）
     */
    @Transactional(readOnly = true)
    public long getProductPageViews(Integer productId) {
        long count = productViewMapper.countByProductId(productId);
        log.info("商品总点击次数 - productId: {}, PV: {}", productId, count);
        return count;
    }

    /**
     * 获取商品的独立访客数（UV）- 多少不同用户点击过
     */
    @Transactional(readOnly = true)
    public long getProductUniqueVisitors(Integer productId) {
        long count = productViewMapper.countUniqueUsersByProductId(productId);
        log.info("商品独立访客数 - productId: {}, UV: {}", productId, count);
        return count;
    }

    /**
     * 获取某用户点击某商品的次数
     */
    @Transactional(readOnly = true)
    public long getUserProductViews(Integer userId, Integer productId) {
        long count = productViewMapper.countByUserIdAndProductId(userId, productId);
        log.info("用户点击商品次数 - userId: {}, productId: {}, 次数: {}", userId, productId, count);
        return count;
    }

    /**
     * 获取商品在指定时间范围内的点击次数
     */
    @Transactional(readOnly = true)
    public long getProductViewsInTimeRange(Integer productId, LocalDateTime startTime, LocalDateTime endTime) {
        long count = productViewMapper.countByProductIdAndTimeRange(productId, startTime, endTime);
        log.info("商品时间范围点击次数 - productId: {}, 时间: {} ~ {}, 次数: {}",
                productId, startTime, endTime, count);
        return count;
    }

    /**
     * 获取用户浏览过的不同商品数量
     */
    @Transactional(readOnly = true)
    public long getUserViewedProductCount(Integer userId) {
        long count = productViewMapper.countDistinctProductsByUserId(userId);
        log.info("用户浏览的商品种类数 - userId: {}, 商品数: {}", userId, count);
        return count;
    }
}

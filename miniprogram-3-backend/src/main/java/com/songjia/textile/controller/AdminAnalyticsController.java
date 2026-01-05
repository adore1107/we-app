package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.*;
import com.songjia.textile.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管理员数据分析控制器
 * 提供各种数据统计和分析功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final BannerMapper bannerMapper;

    /**
     * 获取概览统计数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> data = new HashMap<>();

            // 商品总数
            QueryWrapper<Product> productQuery = new QueryWrapper<>();
            long productCount = productMapper.selectCount(productQuery);
            data.put("productCount", productCount);

            // 上架商品数
            QueryWrapper<Product> onlineProductQuery = new QueryWrapper<>();
            onlineProductQuery.eq("status", true);
            long onlineProductCount = productMapper.selectCount(onlineProductQuery);
            data.put("onlineProductCount", onlineProductCount);

            // 用户总数
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            long userCount = userMapper.selectCount(userQuery);
            data.put("userCount", userCount);

            // 收藏总数
            QueryWrapper<Favorite> favoriteQuery = new QueryWrapper<>();
            long favoriteCount = favoriteMapper.selectCount(favoriteQuery);
            data.put("favoriteCount", favoriteCount);

            // 评论总数
            QueryWrapper<Comment> commentQuery = new QueryWrapper<>();
            long commentCount = commentMapper.selectCount(commentQuery);
            data.put("commentCount", commentCount);

            // 轮播图总数
            QueryWrapper<Banner> bannerQuery = new QueryWrapper<>();
            long bannerCount = bannerMapper.selectCount(bannerQuery);
            data.put("bannerCount", bannerCount);

            log.info("获取概览统计成功");
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取概览统计失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 获取商品分类分布数据
     */
    @GetMapping("/product-category")
    public Result<List<Map<String, Object>>> getProductCategory() {
        try {
            List<Product> products = productMapper.selectList(new QueryWrapper<>());
            
            // 按分类统计
            Map<Integer, Integer> categoryCount = new HashMap<>();
            for (Product product : products) {
                Integer categoryId = product.getMainCategoryId();
                categoryCount.put(categoryId, categoryCount.getOrDefault(categoryId, 0) + 1);
            }

            // 转换为列表
            List<Map<String, Object>> data = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : categoryCount.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("categoryId", entry.getKey());
                item.put("count", entry.getValue());
                data.add(item);
            }

            log.info("获取商品分类分布成功");
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取商品分类分布失败", e);
            return Result.error("获取数据失败");
        }
    }

    /**
     * 获取热门商品排行（按收藏数）
     */
    @GetMapping("/top-products-favorite")
    public Result<List<Map<String, Object>>> getTopProductsByFavorite() {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("favorite_count");
            queryWrapper.last("LIMIT 10");
            
            List<Product> products = productMapper.selectList(queryWrapper);
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", product.getName());
                item.put("favoriteCount", product.getFavoriteCount() != null ? product.getFavoriteCount() : 0);
                data.add(item);
            }

            log.info("获取热门商品排行成功");
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取热门商品排行失败", e);
            return Result.error("获取数据失败");
        }
    }

    /**
     * 获取热门商品排行（按浏览数）
     */
    @GetMapping("/top-products-view")
    public Result<List<Map<String, Object>>> getTopProductsByView() {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("view_count");
            queryWrapper.last("LIMIT 10");
            
            List<Product> products = productMapper.selectList(queryWrapper);
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", product.getName());
                item.put("viewCount", product.getViewCount() != null ? product.getViewCount() : 0);
                data.add(item);
            }

            log.info("获取浏览排行成功");
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取浏览排行失败", e);
            return Result.error("获取数据失败");
        }
    }

    /**
     * 获取商品状态分布
     */
    @GetMapping("/product-status")
    public Result<Map<String, Object>> getProductStatus() {
        try {
            Map<String, Object> data = new HashMap<>();

            // 上架商品数
            QueryWrapper<Product> onlineQuery = new QueryWrapper<>();
            onlineQuery.eq("status", true);
            long onlineCount = productMapper.selectCount(onlineQuery);
            data.put("online", onlineCount);

            // 下架商品数
            QueryWrapper<Product> offlineQuery = new QueryWrapper<>();
            offlineQuery.eq("status", false);
            long offlineCount = productMapper.selectCount(offlineQuery);
            data.put("offline", offlineCount);

            log.info("获取商品状态分布成功");
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取商品状态分布失败", e);
            return Result.error("获取数据失败");
        }
    }

    /**
     * 获取评论评分分布
     */
    @GetMapping("/comment-rating")
    public Result<Map<String, Object>> getCommentRating() {
        try {
            List<Comment> comments = commentMapper.selectList(new QueryWrapper<>());
            
            Map<String, Object> data = new HashMap<>();
            int[] ratings = new int[6]; // 0-5星
            
            for (Comment comment : comments) {
                Integer rating = comment.getRating();
                if (rating != null && rating >= 0 && rating <= 5) {
                    ratings[rating]++;
                }
            }

            data.put("rating1", ratings[1]);
            data.put("rating2", ratings[2]);
            data.put("rating3", ratings[3]);
            data.put("rating4", ratings[4]);
            data.put("rating5", ratings[5]);

            log.info("获取评论评分分布成功");
            return Result.success("获取成功", data);
        } catch (Exception e) {
            log.error("获取评论评分分布失败", e);
            return Result.error("获取数据失败");
        }
    }
}

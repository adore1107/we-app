package com.songjia.textile.controller;

import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Banner;
import com.songjia.textile.entity.Category;
import com.songjia.textile.entity.Product;
import com.songjia.textile.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页控制器 - 提供首页所需的组合数据
 */
@Slf4j
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final ProductService productService;

    /**
     * 获取首页数据
     */
    @GetMapping("/data")
    public Result<Map<String, Object>> getHomeData() {
        try {
            Map<String, Object> homeData = new HashMap<>();

            // 获取首页轮播图（只获取 type='home_banner' 的）
            List<Banner> banners = bannerService.getBannersByType("home_banner");
            homeData.put("banners", banners);

            // 获取分类列表
            List<Category> categories = categoryService.getAllCategories();
            homeData.put("categories", categories);

            // 获取热门商品
            List<Product> hotProducts = productService.getHotProducts();
            homeData.put("hotProducts", hotProducts);

            // 获取新品商品
            List<Product> newProducts = productService.getNewProducts();
            homeData.put("newProducts", newProducts);

            // 获取推荐商品
            List<Product> recommendedProducts = productService.getRecommendedProducts();
            homeData.put("recommendedProducts", recommendedProducts);

            return Result.success("获取首页数据成功", homeData);
        } catch (Exception e) {
            log.error("获取首页数据失败", e);
            return Result.error("获取首页数据失败");
        }
    }

    /**
     * 快速获取首页基础信息
     */
    @GetMapping("/basic")
    public Result<Map<String, Object>> getBasicHomeData() {
        try {
            Map<String, Object> basicData = new HashMap<>();

            // 只获取必要的首页信息（只获取 type='home_banner' 的）
            List<Banner> banners = bannerService.getBannersByType("home_banner");
            basicData.put("banners", banners);

            List<Category> categories = categoryService.getAllCategories();
            basicData.put("categories", categories);

            return Result.success("获取基础首页数据成功", basicData);
        } catch (Exception e) {
            log.error("获取基础首页数据失败", e);
            return Result.error("获取基础首页数据失败");
        }
    }
}
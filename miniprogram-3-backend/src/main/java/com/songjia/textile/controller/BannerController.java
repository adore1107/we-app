package com.songjia.textile.controller;

import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Banner;
import com.songjia.textile.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 */
@Slf4j
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public Result<List<Banner>> getBannerList() {
        try {
            List<Banner> banners = bannerService.getActiveBanners();
            return Result.success("获取成功", banners);
        } catch (Exception e) {
            log.error("获取轮播图列表失败", e);
            return Result.error("获取轮播图失败");
        }
    }

    /**
     * 根据类型获取轮播图
     * @param type 轮播图类型：home_banner-首页轮播，company_gallery-公司风采
     * @return 指定类型的轮播图列表
     */
    @GetMapping("/type/{type}")
    public Result<List<Banner>> getBannersByType(@PathVariable String type) {
        try {
            log.info("获取类型为 {} 的轮播图", type);
            List<Banner> banners = bannerService.getBannersByType(type);
            return Result.success("获取成功", banners);
        } catch (Exception e) {
            log.error("获取类型为 {} 的轮播图失败", type, e);
            return Result.error("获取轮播图失败");
        }
    }
}
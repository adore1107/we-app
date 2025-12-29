package com.songjia.textile.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Banner;
import com.songjia.textile.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerMapper bannerMapper;

    /**
     * 获取所有有效的轮播图
     */
    public List<Banner> getActiveBanners() {
        List<Banner> banners = bannerMapper.findValidBanners(LocalDateTime.now());
        log.info("获取到 {} 个有效轮播图", banners.size());
        return banners;
    }

    /**
     * 获取所有轮播图（管理员用）
     */
    public List<Banner> getAllBanners() {
        List<Banner> banners = bannerMapper.findByStatusOrderBySortOrderAsc(null);
        log.info("获取到 {} 个轮播图（管理员视图）", banners.size());
        return banners;
    }

    /**
     * 分页获取轮播图
     */
    public IPage<Banner> getBannersByPage(int page, int size) {
        Page<Banner> pageable = new Page<>(page + 1, size);
        IPage<Banner> result = bannerMapper.findByStatusOrderBySortOrderAsc(null, pageable);
        log.info("分页获取轮播图: 第 {} 页, 每页 {} 个, 共 {} 条记录",
                page, size, result.getTotal());
        return result;
    }

    /**
     * 获取启用的轮播图
     */
    public List<Banner> getActiveBannersOnly() {
        List<Banner> banners = bannerMapper.findByStatusOrderBySortOrderAsc(true);
        log.info("获取到 {} 个启用的轮播图", banners.size());
        return banners;
    }

    /**
     * 根据ID获取轮播图
     */
    public Banner getBannerById(Integer id) {
        Banner banner = bannerMapper.findById(id);
        if (banner != null) {
            log.info("找到轮播图: id={}, title={}", id, banner.getTitle());
        } else {
            log.warn("未找到轮播图: id={}", id);
        }
        return banner;
    }

    /**
     * 添加轮播图
     */
    @Transactional
    public Banner addBanner(Banner banner) {
        int result = bannerMapper.insert(banner);
        if (result > 0) {
            log.info("成功添加轮播图: title={}", banner.getTitle());
            return banner;
        } else {
            log.error("添加轮播图失败: title={}", banner.getTitle());
            throw new RuntimeException("添加轮播图失败");
        }
    }

    /**
     * 更新轮播图
     */
    @Transactional
    public Banner updateBanner(Banner banner) {
        int result = bannerMapper.updateById(banner);
        if (result > 0) {
            log.info("成功更新轮播图: id={}, title={}", banner.getId(), banner.getTitle());
            return banner;
        } else {
            log.error("更新轮播图失败: id={}", banner.getId());
            throw new RuntimeException("更新轮播图失败");
        }
    }

    /**
     * 删除轮播图（物理删除）
     */
    @Transactional
    public boolean deleteBanner(Integer id) {
        int result = bannerMapper.deleteById(id);
        if (result > 0) {
            log.info("成功删除轮播图: id={}", id);
            return true;
        } else {
            log.error("删除轮播图失败: id={}", id);
            return false;
        }
    }

    /**
     * 根据类型获取轮播图
     * @param type 轮播图类型：home_banner-首页轮播，company_gallery-公司风采
     * @return 指定类型的启用轮播图列表
     */
    public List<Banner> getBannersByType(String type) {
        List<Banner> banners = bannerMapper.findByTypeAndStatus(type, true);
        log.info("获取到 {} 个类型为 {} 的轮播图", banners.size(), type);
        return banners;
    }
}
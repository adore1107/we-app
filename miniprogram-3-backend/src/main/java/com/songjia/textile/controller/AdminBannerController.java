package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Banner;
import com.songjia.textile.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理员轮播图管理控制器
 * 提供轮播图的增删改查等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerMapper bannerMapper;

    /**
     * 获取所有轮播图（管理员版本，包含禁用的轮播图）
     */
    @GetMapping("/list")
    public Result<List<Banner>> getBannerList(@RequestParam(required = false) String type) {
        try {
            QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();

            // 如果指定了类型，则筛选
            if (type != null && !type.trim().isEmpty()) {
                queryWrapper.eq("type", type);
            }

            // 按排序字段降序排列
            queryWrapper.orderByDesc("sort_order", "id");

            List<Banner> banners = bannerMapper.selectList(queryWrapper);
            log.info("管理员获取轮播图列表: type={}, total={}", type, banners.size());
            return Result.success("获取成功", banners);
        } catch (Exception e) {
            log.error("获取轮播图列表失败", e);
            return Result.error("获取轮播图列表失败");
        }
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/detail/{id}")
    public Result<Banner> getBannerDetail(@PathVariable @NotNull Integer id) {
        try {
            Banner banner = bannerMapper.selectById(id);
            if (banner == null) {
                return Result.error("轮播图不存在");
            }
            log.info("管理员获取轮播图详情: id={}", id);
            return Result.success("获取成功", banner);
        } catch (Exception e) {
            log.error("获取轮播图详情失败: id={}", id, e);
            return Result.error("获取轮播图详情失败");
        }
    }

    /**
     * 添加轮播图
     */
    @PostMapping("/add")
    @Transactional
    public Result<Banner> addBanner(@Valid @RequestBody Banner banner) {
        try {
            // 设置默认值
            if (banner.getSortOrder() == null) {
                banner.setSortOrder(0);
            }
            if (banner.getStatus() == null) {
                banner.setStatus(true);
            }
            if (banner.getType() == null || banner.getType().trim().isEmpty()) {
                banner.setType("home_banner");
            }
            if (banner.getLinkType() == null || banner.getLinkType().trim().isEmpty()) {
                banner.setLinkType("none");
            }

            int result = bannerMapper.insert(banner);

            if (result > 0) {
                log.info("添加轮播图成功: id={}, title={}, type={}",
                        banner.getId(), banner.getTitle(), banner.getType());
                return Result.success("添加成功", banner);
            } else {
                log.error("添加轮播图失败: title={}", banner.getTitle());
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            log.error("添加轮播图异常: title={}", banner.getTitle(), e);
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新轮播图
     */
    @PutMapping("/update/{id}")
    @Transactional
    public Result<Banner> updateBanner(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody Banner banner) {
        try {
            // 检查轮播图是否存在
            Banner existingBanner = bannerMapper.selectById(id);
            if (existingBanner == null) {
                return Result.error("轮播图不存在");
            }

            // 设置ID并更新
            banner.setId(id);

            int result = bannerMapper.updateById(banner);

            if (result > 0) {
                // 重新查询返回最新数据
                Banner updatedBanner = bannerMapper.selectById(id);
                log.info("更新轮播图成功: id={}, title={}", id, banner.getTitle());
                return Result.success("更新成功", updatedBanner);
            } else {
                log.error("更新轮播图失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新轮播图异常: id={}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<String> deleteBanner(@PathVariable @NotNull Integer id) {
        try {
            // 检查轮播图是否存在
            Banner banner = bannerMapper.selectById(id);
            if (banner == null) {
                return Result.error("轮播图不存在");
            }

            int result = bannerMapper.deleteById(id);

            if (result > 0) {
                log.info("删除轮播图成功: id={}, title={}", id, banner.getTitle());
                return Result.success("删除成功", "OK");
            } else {
                log.error("删除轮播图失败: id={}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除轮播图异常: id={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除轮播图
     */
    @DeleteMapping("/batch-delete")
    @Transactional
    public Result<String> batchDeleteBanners(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的轮播图");
            }

            int result = bannerMapper.deleteBatchIds(ids);

            log.info("批量删除轮播图: 请求删除{}个, 实际删除{}个", ids.size(), result);

            if (result > 0) {
                return Result.success("删除成功", "成功删除" + result + "个轮播图");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除轮播图异常: ids={}", ids, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新轮播图状态（启用/禁用）
     */
    @PutMapping("/status/{id}")
    @Transactional
    public Result<String> updateBannerStatus(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Boolean status) {
        try {
            Banner banner = bannerMapper.selectById(id);
            if (banner == null) {
                return Result.error("轮播图不存在");
            }

            banner.setStatus(status);
            int result = bannerMapper.updateById(banner);

            if (result > 0) {
                log.info("更新轮播图状态成功: id={}, status={}", id, status);
                return Result.success(status ? "启用成功" : "禁用成功", "OK");
            } else {
                log.error("更新轮播图状态失败: id={}", id);
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新轮播图状态异常: id={}, status={}", id, status, e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新轮播图排序
     */
    @PutMapping("/sort-order/{id}")
    @Transactional
    public Result<String> updateSortOrder(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Integer sortOrder) {
        try {
            Banner banner = bannerMapper.selectById(id);
            if (banner == null) {
                return Result.error("轮播图不存在");
            }

            banner.setSortOrder(sortOrder);
            int result = bannerMapper.updateById(banner);

            if (result > 0) {
                log.info("更新轮播图排序成功: id={}, sortOrder={}", id, sortOrder);
                return Result.success("更新成功", "OK");
            } else {
                log.error("更新轮播图排序失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新轮播图排序异常: id={}, sortOrder={}", id, sortOrder, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}

package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 轮播图数据访问接口
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    /**
     * 根据状态查找轮播图
     */
    List<Banner> findByStatusOrderBySortOrderAsc(@Param("status") Boolean status);

    /**
     * 分页根据状态查找轮播图
     */
    IPage<Banner> findByStatusOrderBySortOrderAsc(@Param("status") Boolean status, Page<Banner> page);

    /**
     * 查找当前有效的轮播图
     */
    List<Banner> findValidBanners(@Param("currentTime") java.time.LocalDateTime currentTime);

    /**
     * 根据ID查找轮播图
     */
    Banner findById(@Param("id") Integer id);

    /**
     * 根据类型和状态查找轮播图
     * @param type 轮播图类型
     * @param status 状态
     * @return 轮播图列表（按sort_order排序）
     */
    List<Banner> findByTypeAndStatus(@Param("type") String type, @Param("status") Boolean status);
}
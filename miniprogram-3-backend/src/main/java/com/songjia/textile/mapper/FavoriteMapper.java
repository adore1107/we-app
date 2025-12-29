package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏数据访问接口
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 根据用户ID查找收藏列表
     */
    List<Favorite> findByUserId(@Param("userId") Integer userId);

    /**
     * 分页根据用户ID查找收藏列表
     */
    IPage<Favorite> findByUserId(@Param("userId") Integer userId, Page<Favorite> page);

    /**
     * 根据用户ID和商品ID查找收藏（只查询未删除的）
     */
    Favorite findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    /**
     * 根据用户ID和商品ID查找收藏（包括已软删除的记录）
     */
    Favorite findByUserIdAndProductIdIncludeDeleted(@Param("userId") Integer userId, @Param("productId") Integer productId);

    /**
     * 统计用户收藏数量
     */
    int countByUserId(@Param("userId") Integer userId);

    /**
     * 软删除用户收藏
     */
    int deleteByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    /**
     * 物理删除用户收藏（管理员用）
     */
    int physicalDeleteById(@Param("id") Integer id);

    /**
     * 恢复软删除的收藏
     */
    int restoreFavorite(@Param("userId") Integer userId, @Param("productId") Integer productId);
}
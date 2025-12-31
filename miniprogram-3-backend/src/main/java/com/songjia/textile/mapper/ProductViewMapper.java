package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.songjia.textile.entity.ProductView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 商品浏览记录Mapper接口
 */
@Mapper
public interface ProductViewMapper extends BaseMapper<ProductView> {

    /**
     * 统计商品的总点击次数（PV）
     */
    @Select("SELECT COUNT(*) FROM product_views WHERE product_id = #{productId}")
    long countByProductId(@Param("productId") Integer productId);

    /**
     * 统计商品的独立访客数（UV）- 多少不同用户点击过
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM product_views WHERE product_id = #{productId}")
    long countUniqueUsersByProductId(@Param("productId") Integer productId);

    /**
     * 统计某用户点击某商品的次数
     */
    @Select("SELECT COUNT(*) FROM product_views WHERE user_id = #{userId} AND product_id = #{productId}")
    long countByUserIdAndProductId(
            @Param("userId") Integer userId,
            @Param("productId") Integer productId
    );

    /**
     * 统计商品在指定时间范围内的点击次数
     */
    @Select("SELECT COUNT(*) FROM product_views " +
            "WHERE product_id = #{productId} AND created_at BETWEEN #{startTime} AND #{endTime}")
    long countByProductIdAndTimeRange(
            @Param("productId") Integer productId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户浏览过的不同商品数量
     */
    @Select("SELECT COUNT(DISTINCT product_id) FROM product_views WHERE user_id = #{userId}")
    long countDistinctProductsByUserId(@Param("userId") Integer userId);
}

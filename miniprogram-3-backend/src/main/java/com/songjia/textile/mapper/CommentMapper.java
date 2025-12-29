package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论数据访问接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据商品ID查找评论列表（分页，包含用户信息）
     */
    IPage<Comment> findByProductIdWithUser(@Param("productId") Integer productId, Page<Comment> page);

    /**
     * 根据商品ID查找评论列表（不分页，包含用户信息）
     */
    List<Comment> findByProductIdWithUser(@Param("productId") Integer productId);

    /**
     * 根据用户ID查找评论列表
     */
    List<Comment> findByUserId(@Param("userId") Integer userId);

    /**
     * 统计商品评论数量
     */
    int countByProductId(@Param("productId") Integer productId);

    /**
     * 获取商品平均评分
     */
    Double getAverageRating(@Param("productId") Integer productId);

    /**
     * 商家回复评论
     */
    int replyComment(@Param("id") Integer id, @Param("adminReply") String adminReply);
}

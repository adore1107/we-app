package com.songjia.textile.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Comment;
import com.songjia.textile.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 评论服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentMapper commentMapper;

    /**
     * 添加评论
     */
    public Comment addComment(Integer userId, Integer productId, String content, Integer rating) {
        // 验证评分范围
        if (rating == null || rating < 1 || rating > 5) {
            rating = 5; // 默认5星
        }

        // 创建评论
        Comment comment = Comment.builder()
                .userId(userId)
                .productId(productId)
                .content(content)
                .rating(rating)
                .status(true) // 默认显示
                .deleted(0)
                .build();

        int result = commentMapper.insert(comment);
        if (result <= 0) {
            throw new RuntimeException("添加评论失败");
        }

        log.info("用户添加评论: userId={}, productId={}, rating={}", userId, productId, rating);
        return comment;
    }

    /**
     * 删除评论（软删除）
     */
    public void deleteComment(Integer id, Integer userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        int result = commentMapper.deleteById(id);
        if (result <= 0) {
            throw new RuntimeException("删除评论失败");
        }

        log.info("用户删除评论: userId={}, commentId={}", userId, id);
    }

    /**
     * 获取商品评论列表（分页，包含用户信息）
     */
    @Transactional(readOnly = true)
    public IPage<Comment> getProductComments(Integer productId, int page, int size) {
        Page<Comment> pageable = new Page<>(page + 1, size);
        IPage<Comment> result = commentMapper.findByProductIdWithUser(productId, pageable);
        log.info("获取商品评论列表: productId={}, 第 {} 页, 每页 {} 个, 共 {} 条记录",
                productId, page, size, result.getTotal());
        return result;
    }

    /**
     * 获取商品所有评论（不分页，包含用户信息）
     */
    @Transactional(readOnly = true)
    public List<Comment> getAllProductComments(Integer productId) {
        List<Comment> comments = commentMapper.findByProductIdWithUser(productId);
        log.info("获取商品所有评论: productId={}, 数量={}", productId, comments.size());
        return comments;
    }

    /**
     * 获取用户的评论列表
     */
    @Transactional(readOnly = true)
    public List<Comment> getUserComments(Integer userId) {
        List<Comment> comments = commentMapper.findByUserId(userId);
        log.info("获取用户评论列表: userId={}, 数量={}", userId, comments.size());
        return comments;
    }

    /**
     * 统计商品评论数量
     */
    @Transactional(readOnly = true)
    public int getCommentCount(Integer productId) {
        int count = commentMapper.countByProductId(productId);
        log.info("商品评论数量: productId={}, count={}", productId, count);
        return count;
    }

    /**
     * 获取商品平均评分
     */
    @Transactional(readOnly = true)
    public Double getAverageRating(Integer productId) {
        Double avgRating = commentMapper.getAverageRating(productId);
        log.info("商品平均评分: productId={}, avgRating={}", productId, avgRating);
        return avgRating != null ? avgRating : 0.0;
    }

    /**
     * 商家回复评论
     */
    public void replyComment(Integer id, String adminReply) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        int result = commentMapper.replyComment(id, adminReply);
        if (result <= 0) {
            throw new RuntimeException("回复评论失败");
        }

        log.info("商家回复评论: commentId={}, reply={}", id, adminReply);
    }

    /**
     * 根据ID获取评论
     */
    @Transactional(readOnly = true)
    public Optional<Comment> getCommentById(Integer id) {
        Comment comment = commentMapper.selectById(id);
        return Optional.ofNullable(comment);
    }

    /**
     * 隐藏/显示评论
     */
    public void toggleCommentStatus(Integer id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        comment.setStatus(!comment.getStatus());
        commentMapper.updateById(comment);

        log.info("切换评论状态: commentId={}, newStatus={}", id, comment.getStatus());
    }
}

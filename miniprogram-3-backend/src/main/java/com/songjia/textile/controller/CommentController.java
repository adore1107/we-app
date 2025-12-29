package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Comment;
import com.songjia.textile.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public Result<Comment> addComment(@RequestParam @NotNull Integer userId,
                                       @RequestParam @NotNull Integer productId,
                                       @RequestParam @NotBlank String content,
                                       @RequestParam(defaultValue = "5") Integer rating) {
        try {
            log.info("添加评论请求: userId={}, productId={}, rating={}", userId, productId, rating);
            Comment comment = commentService.addComment(userId, productId, content, rating);
            return Result.success("评论成功", comment);
        } catch (Exception e) {
            log.error("添加评论失败: userId={}, productId={}", userId, productId, e);
            return Result.error("评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteComment(@PathVariable @NotNull Integer id,
                                         @RequestParam @NotNull Integer userId) {
        try {
            commentService.deleteComment(id, userId);
            return Result.success("删除成功", "删除成功");
        } catch (Exception e) {
            log.error("删除评论失败: id={}, userId={}", id, userId, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品评论列表（分页）
     */
    @GetMapping("/product/{productId}")
    public Result<IPage<Comment>> getProductComments(@PathVariable @NotNull Integer productId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        try {
            IPage<Comment> comments = commentService.getProductComments(productId, page, size);
            return Result.success("获取成功", comments);
        } catch (Exception e) {
            log.error("获取商品评论列表失败: productId={}", productId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品所有评论（不分页）
     */
    @GetMapping("/product/{productId}/all")
    public Result<List<Comment>> getAllProductComments(@PathVariable @NotNull Integer productId) {
        try {
            List<Comment> comments = commentService.getAllProductComments(productId);
            return Result.success("获取成功", comments);
        } catch (Exception e) {
            log.error("获取商品所有评论失败: productId={}", productId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户评论列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Comment>> getUserComments(@PathVariable @NotNull Integer userId) {
        try {
            List<Comment> comments = commentService.getUserComments(userId);
            return Result.success("获取成功", comments);
        } catch (Exception e) {
            log.error("获取用户评论列表失败: userId={}", userId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品评论统计（数量 + 平均评分）
     */
    @GetMapping("/product/{productId}/stats")
    public Result<Map<String, Object>> getCommentStats(@PathVariable @NotNull Integer productId) {
        try {
            int count = commentService.getCommentCount(productId);
            Double avgRating = commentService.getAverageRating(productId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("count", count);
            stats.put("avgRating", Math.round(avgRating * 10) / 10.0); // 保留1位小数

            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取评论统计失败: productId={}", productId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 商家回复评论（需要后台管理系统，暂不实现前端）
     */
    @PostMapping("/reply/{id}")
    public Result<String> replyComment(@PathVariable @NotNull Integer id,
                                        @RequestParam @NotBlank String adminReply) {
        try {
            commentService.replyComment(id, adminReply);
            return Result.success("回复成功", "回复成功");
        } catch (Exception e) {
            log.error("回复评论失败: id={}", id, e);
            return Result.error("回复失败: " + e.getMessage());
        }
    }

    /**
     * 隐藏/显示评论（管理员功能，需要后台管理系统）
     */
    @PutMapping("/toggle/{id}")
    public Result<String> toggleCommentStatus(@PathVariable @NotNull Integer id) {
        try {
            commentService.toggleCommentStatus(id);
            return Result.success("操作成功", "操作成功");
        } catch (Exception e) {
            log.error("切换评论状态失败: id={}", id, e);
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}



package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Comment;
import com.songjia.textile.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员评论管理控制器
 * 提供评论的查询、回复、删除等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentMapper commentMapper;

    /**
     * 获取评论列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<Comment>> getCommentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Boolean status) {
        try {
            Page<Comment> pageable = new Page<>(page + 1, size);
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

            // 筛选条件
            if (productId != null) {
                queryWrapper.eq("product_id", productId);
            }
            if (userId != null) {
                queryWrapper.eq("user_id", userId);
            }
            if (status != null) {
                queryWrapper.eq("status", status);
            }

            // 只查询未删除的评论
            queryWrapper.eq("deleted", 0);

            // 按创建时间降序排列
            queryWrapper.orderByDesc("created_at", "id");

            IPage<Comment> comments = commentMapper.selectPage(pageable, queryWrapper);

            log.info("管理员获取评论列表: productId={}, userId={}, status={}, total={}",
                    productId, userId, status, comments.getTotal());

            return Result.success("获取成功", comments);
        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return Result.error("获取评论列表失败");
        }
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/detail/{id}")
    public Result<Comment> getCommentDetail(@PathVariable @NotNull Integer id) {
        try {
            Comment comment = commentMapper.selectById(id);
            if (comment == null || comment.getDeleted() == 1) {
                return Result.error("评论不存在");
            }
            log.info("管理员获取评论详情: id={}", id);
            return Result.success("获取成功", comment);
        } catch (Exception e) {
            log.error("获取评论详情失败: id={}", id, e);
            return Result.error("获取评论详情失败");
        }
    }

    /**
     * 回复评论
     */
    @PutMapping("/reply/{id}")
    @Transactional
    public Result<String> replyComment(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull String adminReply) {
        try {
            Comment comment = commentMapper.selectById(id);
            if (comment == null || comment.getDeleted() == 1) {
                return Result.error("评论不存在");
            }

            comment.setAdminReply(adminReply);
            comment.setReplyTime(LocalDateTime.now());
            int result = commentMapper.updateById(comment);

            if (result > 0) {
                log.info("回复评论成功: id={}", id);
                return Result.success("回复成功", "OK");
            } else {
                log.error("回复评论失败: id={}", id);
                return Result.error("回复失败");
            }
        } catch (Exception e) {
            log.error("回复评论异常: id={}", id, e);
            return Result.error("回复失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论（软删除）
     */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<String> deleteComment(@PathVariable @NotNull Integer id) {
        try {
            Comment comment = commentMapper.selectById(id);
            if (comment == null) {
                return Result.error("评论不存在");
            }

            // 使用 MyBatis-Plus 的 deleteById 会自动软删除（因为有 @TableLogic 注解）
            int result = commentMapper.deleteById(id);

            if (result > 0) {
                log.info("删除评论成功: id={}", id);
                return Result.success("删除成功", "OK");
            } else {
                log.error("删除评论失败: id={}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除评论异常: id={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除评论（软删除）
     */
    @DeleteMapping("/batch-delete")
    @Transactional
    public Result<String> batchDeleteComments(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的评论");
            }

            // 使用 MyBatis-Plus 的 deleteBatchIds 会自动软删除（因为有 @TableLogic 注解）
            int result = commentMapper.deleteBatchIds(ids);

            log.info("批量删除评论: 请求删除{}个, 实际删除{}个", ids.size(), result);

            if (result > 0) {
                return Result.success("删除成功", "成功删除" + result + "个评论");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除评论异常: ids={}", ids, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新评论状态（显示/隐藏）
     */
    @PutMapping("/status/{id}")
    @Transactional
    public Result<String> updateCommentStatus(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Boolean status) {
        try {
            Comment comment = commentMapper.selectById(id);
            if (comment == null || comment.getDeleted() == 1) {
                return Result.error("评论不存在");
            }

            comment.setStatus(status);
            int result = commentMapper.updateById(comment);

            if (result > 0) {
                log.info("更新评论状态成功: id={}, status={}", id, status);
                return Result.success(status ? "显示成功" : "隐藏成功", "OK");
            } else {
                log.error("更新评论状态失败: id={}", id);
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新评论状态异常: id={}, status={}", id, status, e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
}

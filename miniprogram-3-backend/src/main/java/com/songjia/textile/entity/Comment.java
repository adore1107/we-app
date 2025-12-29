package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 商品评论实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("product_id")
    private Integer productId;

    private String content;

    private Integer rating; // 评分 1-5星

    @TableField("admin_reply")
    private String adminReply; // 商家回复

    @TableField("reply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    private Boolean status; // 状态：0-隐藏，1-显示

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 软删除字段：0-正常，1-已删除
    @TableLogic
    private Integer deleted;

    // 关联用户信息（用于前端显示，不存数据库）
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;
}

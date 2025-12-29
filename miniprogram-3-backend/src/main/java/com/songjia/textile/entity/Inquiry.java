package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 询价记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("inquiries")
public class Inquiry {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer productId;

    private Integer quantity;

    private String message;

    private Integer status; // 0-待处理 1-已处理

    private String adminReply;

    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
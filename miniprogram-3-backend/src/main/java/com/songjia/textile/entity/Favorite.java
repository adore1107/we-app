package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("favorites")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer productId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 软删除字段：0-正常，1-已删除
    @TableLogic
    private Integer deleted;
}
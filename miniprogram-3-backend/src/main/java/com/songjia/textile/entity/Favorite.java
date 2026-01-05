package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @TableField("user_id")
    private Integer userId;

    @TableField("product_id")
    private Integer productId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 软删除字段：0-正常，1-已删除
    @TableLogic
    private Integer deleted;

    // 关联商品信息（用于前端显示，不存数据库）
    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productImage;

    @TableField(exist = false)
    private Double productPrice;

    @TableField(exist = false)
    private Integer productStock;

    @TableField(exist = false)
    private Boolean productStatus;

    // 关联用户信息（用于前端显示，不存数据库）
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userPhone;
}
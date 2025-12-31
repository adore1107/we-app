package com.songjia.textile.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 商品浏览记录实体类
 * 记录每次用户浏览商品的行为，包括浏览时长
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_views")
public class ProductView {

    /**
     * 浏览记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Integer productId;

    /**
     * 进入页面时间
     */
    @TableField("view_start_time")
    private LocalDateTime viewStartTime;

    /**
     * 离开页面时间
     */
    @TableField("view_end_time")
    private LocalDateTime viewEndTime;

    /**
     * 浏览时长（秒）
     */
    @TableField("duration_seconds")
    private Integer durationSeconds;

    /**
     * 记录创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

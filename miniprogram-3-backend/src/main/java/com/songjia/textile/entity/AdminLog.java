package com.songjia.textile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员操作日志实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin_logs")
public class AdminLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 管理员ID
     */
    @TableField("admin_id")
    private Integer adminId;

    /**
     * 管理员用户名（冗余字段）
     */
    @TableField("admin_name")
    private String adminName;

    /**
     * 模块：product-商品，category-分类，banner-轮播图等
     */
    private String module;

    /**
     * 操作类型：create-新增，update-修改，delete-删除，login-登录等
     */
    private String action;

    /**
     * 操作内容（JSON格式）
     */
    private String content;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 浏览器UA
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 操作时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

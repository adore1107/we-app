package com.songjia.textile.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("admins")
public class Admin {

    /**
     * 管理员ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 角色：super_admin-超级管理员，product_admin-商品管理员，customer_service-客服，analyst-数据分析员
     */
    private String role;

    /**
     * 状态：0-禁用，1-启用
     */
    private Boolean status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

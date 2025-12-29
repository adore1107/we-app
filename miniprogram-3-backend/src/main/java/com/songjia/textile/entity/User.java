package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String openid;

    private String nickname;

      @TableField("avatar_url")
    private String avatarUrl;

    private String phone;

    @TableField("company_name")
    private String companyName;

  @TableField("real_name")
    private String realName;

  @TableField("position")
    private String position;

  @TableField("industry")
    private String industry;

  @TableField("city")
    private String city;

  @TableField("created_at")
  private LocalDateTime createdAt;

  @TableField("last_login_at")
  private LocalDateTime lastLoginAt;
}
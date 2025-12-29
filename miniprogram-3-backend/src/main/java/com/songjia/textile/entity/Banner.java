package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("banners")
public class Banner {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 轮播图类型：home_banner-首页轮播，company_gallery-公司风采，product_banner-商品轮播
     */
    private String type;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private String linkType;

    private Integer sortOrder;

    private Boolean status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 轮播图类型枚举
    public enum BannerType {
        home_banner,       // 首页轮播图
        company_gallery,   // 公司风采图片
        product_banner     // 商品详情轮播图
    }

    // 链接类型枚举
    public enum LinkType {
        none,       // 无链接
        product,    // 商品详情
        category,   // 分类页面
        url         // 外部链接
    }
}
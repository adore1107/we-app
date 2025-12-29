package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String mainImage;

    private String images; // JSON数组格式存储

    private String description;

    private String features; // JSON数组格式存储

    private String specifications; // JSON对象格式存储

    @TableField(exist = false) // 数据库中不存在此字段，仅用于业务逻辑
    private Integer stock = 999; // 库存数量，B2B场景默认充足

    private Integer minOrderQuantity;

    private String unit;

    private Integer leadTime; // 交货周期（天）

    private Double wholesalePrice;

    private Double retailPrice;

    private Integer sortOrder;

    private Boolean isHot;

    private Boolean isNew;

    private Boolean isRecommended;

    private Boolean status;

    private Integer viewCount;

    private Integer favoriteCount;

    /**
     * 一级分类ID（必填）
     */
    private Integer mainCategoryId;

    /**
     * 二级分类ID（可选）
     */
    private Integer subCategoryId;

    // 注释掉关联关系，后续可以通过手动查询获取
    // private List<Favorite> favorites;
    // private List<Inquiry> inquiries;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
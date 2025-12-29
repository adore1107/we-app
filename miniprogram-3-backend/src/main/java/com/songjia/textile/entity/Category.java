package com.songjia.textile.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类实体类（支持二级分类）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("categories")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 父分类ID，NULL表示一级分类
     */
    private Integer parentId;

    /**
     * 分类层级：1=一级分类，2=二级分类
     */
    private Integer level;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private Boolean status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 子分类列表（不映射到数据库，仅用于返回树形结构）
     */
    @TableField(exist = false)
    private List<Category> children;
}
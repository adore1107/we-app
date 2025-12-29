package com.songjia.textile.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品搜索DTO
 */
@Data
public class ProductSearchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Integer spuId;

    /**
     * 商品名称
     */
    private String title;

    /**
     * 商品主图
     */
    private String primaryImage;

    /**
     * 最低批发价
     */
    private BigDecimal minSalePrice;

    /**
     * 最高批发价
     */
    private BigDecimal maxSalePrice;

    /**
     * 起订量
     */
    private Integer minOrderQuantity;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 供应商名称
     */
    private String storeName;

    /**
     * 总收藏数
     */
    private Integer favoriteCount;

    /**
     * 总销量
     */
    private Integer totalSales;

    /**
     * 商品标签
     */
    private String tags;
}
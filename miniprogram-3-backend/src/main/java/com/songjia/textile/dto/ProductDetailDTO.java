package com.songjia.textile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 商品详情DTO - 专为前端详情页设计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDTO {

    private Integer spuId;
    private String title;
    private String description; // 商品描述
    private List<String> desc;  // 商品详情图片列表
    private String primaryImage;
    private List<String> images;

    // 价格信息（分为单位）
    private Integer price;
    private Integer originPrice;
    private Integer minSalePrice;
    private Integer maxSalePrice;
    private Integer maxLinePrice;

    // B2B字段
    private Integer minOrderQuantity;
    private String unit;
    private Integer leadTime;
    private Boolean available;
    private Integer isPutOnSale;
    private Integer spuStockQuantity;
    private Integer soldNum;

    // SKU信息
    private List<SkuInfo> skuList;
    private List<SpecInfo> specList;

    // 规格参数列表
    private List<SpecificationParam> specificationParams;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkuInfo {
        private Integer skuId;
        private Integer price;
        private StockInfo stockInfo;
        private String specInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockInfo {
        private Integer stockQuantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecInfo {
        private String specTitle;
        private List<String> specValues;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpecificationParam {
        private String key;        // 规格参数键名（如 model, fabric_composition）
        private String name;       // 显示名称（如 型号, 面料参数）
        private String value;      // 规格参数值
        private Integer order;     // 显示顺序
    }

    /**
     * 从Product实体转换为DTO
     */
    public static ProductDetailDTO fromProduct(com.songjia.textile.entity.Product product) {
        // 解析图片JSON数组
        List<String> imageList = new ArrayList<>();

        // 总是包含主图
        if (product.getMainImage() != null && !product.getMainImage().isEmpty()) {
            imageList.add(product.getMainImage());
        }

        // 解析images字段的JSON数组
        if (product.getImages() != null) {
            try {
                // 直接使用Jackson解析JSON数组
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String[] imageArray = objectMapper.readValue(product.getImages().toString(), String[].class);
                for (String image : imageArray) {
                    if (image != null && !image.isEmpty()) {
                        imageList.add(image);
                    }
                }
            } catch (Exception e) {
                // JSON解析失败时跳过，只使用主图
                System.err.println("解析images JSON失败，使用主图: " + e.getMessage());
            }
        }

        // 创建SKU信息
        List<SkuInfo> skuList = List.of(
            SkuInfo.builder()
                .skuId(product.getId())
                .price(product.getWholesalePrice() != null ? product.getWholesalePrice().intValue() * 100 : 0) // 转换为分
                .stockInfo(StockInfo.builder()
                    .stockQuantity(product.getStock() != null ? product.getStock() : 999)
                    .build())
                .build()
        );

        // 处理规格参数
        List<SpecificationParam> specificationParams = parseSpecificationParams(product.getSpecifications());

        return ProductDetailDTO.builder()
            .spuId(product.getId())
            .title(product.getName())
            .description(product.getDescription()) // 添加商品描述
            .desc(imageList) // 详情图片使用商品图片
            .primaryImage(product.getMainImage())
            .images(imageList)
            .price(product.getWholesalePrice() != null ? product.getWholesalePrice().intValue() * 100 : 0)
            .originPrice(product.getRetailPrice() != null ? product.getRetailPrice().intValue() * 100 : 0)
            .minSalePrice(product.getWholesalePrice() != null ? product.getWholesalePrice().intValue() * 100 : 0)
            .maxSalePrice(product.getWholesalePrice() != null ? product.getWholesalePrice().intValue() * 100 : 0)
            .maxLinePrice(product.getRetailPrice() != null ? product.getRetailPrice().intValue() * 100 : 0)
            .minOrderQuantity(product.getMinOrderQuantity() != null ? product.getMinOrderQuantity() : 100)
            .unit(product.getUnit() != null ? product.getUnit() : "件")
            .leadTime(product.getLeadTime() != null ? product.getLeadTime() : 7)
            .available(product.getStatus())
            .isPutOnSale(product.getStatus() ? 1 : 0)
            .spuStockQuantity(product.getStock() != null ? product.getStock() : 999)
            .soldNum(product.getViewCount() != null ? product.getViewCount() : 0)
            .skuList(skuList)
            .specList(List.of()) // 暂时为空，后续可根据规格信息生成
            .specificationParams(specificationParams)
            .build();
    }

    /**
     * 解析规格参数JSON字符串
     */
    private static List<SpecificationParam> parseSpecificationParams(String specificationsJson) {
        List<SpecificationParam> params = new ArrayList<>();

        if (specificationsJson == null || specificationsJson.trim().isEmpty()) {
            return params;
        }

        try {
            System.out.println("=== 开始解析规格参数 ===");
            System.out.println("原始JSON字符串: " + specificationsJson);

            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(specificationsJson);

            System.out.println("JSON解析成功，根节点类型: " + rootNode.getNodeType());
            System.out.println("根节点字段数量: " + rootNode.size());

            // 定义参数显示名称和顺序的映射
            final java.util.Map<String, String> nameMapping = java.util.Map.of(
                "model", "型号",
                "fabric_composition", "面料参数",
                "weaving_process", "织造工艺",
                "finishing_process", "后处理工艺",
                "width", "门幅",
                "weight", "克重",
                "density", "密度",
                "color_fastness", "色牢度",
                "shrinkage", "缩水率",
                "function", "功能特性"
            );

            final java.util.Map<String, Integer> orderMapping = java.util.Map.of(
                "model", 1,
                "fabric_composition", 2,
                "weaving_process", 3,
                "finishing_process", 4,
                "width", 5,
                "weight", 6,
                "density", 7,
                "color_fastness", 8,
                "shrinkage", 9,
                "function", 10
            );

            int index = 0;
            for (java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
                java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = it.next();
                String key = entry.getKey();
                String value = entry.getValue().asText();

                if (value != null && !value.trim().isEmpty()) {
                    String displayName = nameMapping.getOrDefault(key, key);
                    Integer order = orderMapping.getOrDefault(key, 999);

                    params.add(SpecificationParam.builder()
                        .key(key)
                        .name(displayName)
                        .value(value)
                        .order(order)
                        .build());
                }
            }

            // 按order字段排序
            params.sort((a, b) -> a.getOrder().compareTo(b.getOrder()));

            System.out.println("规格参数解析完成，共 " + params.size() + " 个参数:");
            for (int i = 0; i < params.size(); i++) {
                SpecificationParam param = params.get(i);
                System.out.println(String.format("  [%d] key=%s, name=%s, value=%s",
                    i, param.getKey(), param.getName(), param.getValue()));
            }

        } catch (Exception e) {
            // JSON解析失败时，返回空列表
            System.err.println("解析规格参数失败: " + e.getMessage());
            e.printStackTrace();
        }

        return params;
    }
}
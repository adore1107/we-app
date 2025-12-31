package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.common.Result;
import com.songjia.textile.dto.ProductDetailDTO;
import com.songjia.textile.entity.Product;
import com.songjia.textile.service.ProductService;
import com.songjia.textile.service.ProductViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductViewService productViewService;

    /**
     * 获取商品列表（分页）
     * 支持同时按分类和关键词筛选，支持排序
     */
    @GetMapping("/list")
    public Result<IPage<Product>> getProductList(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "overall") String sortType) {
        try {
            IPage<Product> products;

            // 同时有分类和关键词：在指定分类中搜索
            if (categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
                log.info("分类内搜索商品: categoryId={}, keyword={}, sortType={}, page={}, size={}", categoryId, keyword, sortType, page, size);
                products = productService.searchProductsInCategory(categoryId, keyword.trim(), page, size, sortType);
            }
            // 只有关键词：全局搜索
            else if (keyword != null && !keyword.trim().isEmpty()) {
                log.info("全局搜索商品: keyword={}, sortType={}, page={}, size={}", keyword, sortType, page, size);
                products = productService.searchProductsByName(keyword.trim(), page, size, sortType);
            }
            // 只有分类：获取分类下所有商品
            else if (categoryId != null) {
                log.info("获取分类商品: categoryId={}, sortType={}, page={}, size={}", categoryId, sortType, page, size);
                products = productService.getProductsByCategory(categoryId, page, size, sortType);
            }
            // 都没有：获取所有商品
            else {
                log.info("获取所有商品: sortType={}, page={}, size={}", sortType, page, size);
                products = productService.getAllProducts(page, size, sortType);
            }

            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取商品列表失败: keyword={}, categoryId={}, sortType={}", keyword, categoryId, sortType, e);
            return Result.error("获取商品列表失败");
        }
    }

    /**
     * 获取商品详情
     * 支持智能去重的浏览统计（同一用户同一天只计1次）
     */
    @GetMapping("/detail/{id}")
    public Result<ProductDetailDTO> getProductDetail(
            @PathVariable @NotNull Integer id,
            @RequestParam(required = false) Integer userId) {
        try {
            log.info("获取商品详情请求: id={}, userId={}", id, userId);

            Optional<Product> productOpt = productService.getProductById(id);

            if (productOpt.isEmpty()) {
                log.warn("商品不存在: id={}", id);
                return Result.error("商品不存在");
            }

            Product product = productOpt.get();

            if (!product.getStatus()) {
                log.warn("商品已下架: id={}, status={}", id, product.getStatus());
                return Result.error("商品已下架");
            }

            // 记录浏览
            if (userId != null) {
                // 记录用户浏览（每次都新增记录）
                productViewService.recordProductView(userId, id);
            } else {
                // 兼容旧版本：如果没有userId，只更新总浏览量
                productService.incrementViewCount(id);
                log.warn("未提供userId，无法记录用户浏览行为");
            }

            // 转换为DTO格式
            log.info("开始转换商品详情DTO - 商品ID: {}, 规格: {}", product.getId(), product.getSpecifications());
            ProductDetailDTO detailDTO = ProductDetailDTO.fromProduct(product);
            log.info("DTO转换完成 - 规格参数数量: {}", detailDTO.getSpecificationParams() != null ? detailDTO.getSpecificationParams().size() : 0);
            if (detailDTO.getSpecificationParams() != null) {
                for (int i = 0; i < detailDTO.getSpecificationParams().size(); i++) {
                    ProductDetailDTO.SpecificationParam param = detailDTO.getSpecificationParams().get(i);
                    log.info("规格参数[{}]: key={}, name={}, value={}", i, param.getKey(), param.getName(), param.getValue());
                }
            }

            log.info("商品详情转换完成: spuId={}, title={}, minOrderQuantity={}",
                    detailDTO.getSpuId(), detailDTO.getTitle(), detailDTO.getMinOrderQuantity());

            return Result.success("获取成功", detailDTO);
        } catch (Exception e) {
            log.error("获取商品详情失败: id={}", id, e);
            return Result.error("获取商品详情失败");
        }
    }

    /**
     * 根据分类获取商品（分页）
     */
    @GetMapping("/category/{categoryId}")
    public Result<IPage<Product>> getProductsByCategory(
            @PathVariable @NotNull Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        try {
            log.info("=== 开始查询分类商品 ===");
            log.info("请求参数 - categoryId: {}, page: {}, size: {}", categoryId, page, size);

            IPage<Product> products = productService.getProductsByCategory(categoryId, page, size);

            log.info("查询结果 - 返回商品数量: {}, 总数量: {}, 当前页: {}, 总页数: {}",
                    products.getRecords().size(), products.getTotal(), products.getCurrent(), products.getPages());

            // 打印商品ID用于检查重复
            if (!products.getRecords().isEmpty()) {
                List<Integer> productIds = products.getRecords().stream()
                        .map(Product::getId)
                        .toList();
                log.info("返回的商品ID列表: {}", productIds);
            }

            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("根据分类获取商品失败: categoryId={}", categoryId, e);
            return Result.error("获取商品失败");
        }
    }

    /**
     * 获取分类商品数量统计
     */
    @GetMapping("/category/{categoryId}/count")
    public Result<Long> getCategoryProductCount(@PathVariable @NotNull Integer categoryId) {
        try {
            long count = productService.getCategoryProductCount(categoryId);
            return Result.success("获取成功", count);
        } catch (Exception e) {
            log.error("获取分类商品数量失败: categoryId={}", categoryId, e);
            return Result.error("获取分类商品数量失败");
        }
    }

    /**
     * 获取分类下的热门商品
     */
    @GetMapping("/category/{categoryId}/hot")
    public Result<IPage<Product>> getCategoryHotProducts(
            @PathVariable @NotNull Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        try {
            IPage<Product> products = productService.getCategoryHotProducts(categoryId, page, size);
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取分类热门商品失败: categoryId={}", categoryId, e);
            return Result.error("获取分类热门商品失败");
        }
    }

    /**
     * 获取分类下的新品商品
     */
    @GetMapping("/category/{categoryId}/new")
    public Result<IPage<Product>> getCategoryNewProducts(
            @PathVariable @NotNull Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        try {
            IPage<Product> products = productService.getCategoryNewProducts(categoryId, page, size);
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取分类新品商品失败: categoryId={}", categoryId, e);
            return Result.error("获取分类新品商品失败");
        }
    }

    /**
     * 根据分类获取商品（不分页，兼容旧接口）
     */
    @GetMapping("/category/{categoryId}/all")
    public Result<List<Product>> getAllProductsByCategory(@PathVariable @NotNull Integer categoryId) {
        try {
            List<Product> products = productService.getProductsByCategory(categoryId);
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("根据分类获取商品失败: categoryId={}", categoryId, e);
            return Result.error("获取商品失败");
        }
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<IPage<Product>> searchProducts(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        try {
            IPage<Product> products = productService.searchProducts(keyword, page, size);
            return Result.success("搜索成功", products);
        } catch (Exception e) {
            log.error("搜索商品失败: keyword={}", keyword, e);
            return Result.error("搜索失败");
        }
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<IPage<Product>> getHotProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            // MyBatis-Plus的Page对象使用从1开始的页码，前端传递从0开始的页码，所以需要+1
            Page<Product> pageable = new Page<>(page + 1, size);
            IPage<Product> products = productService.getHotProducts(pageable);
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取热门商品失败", e);
            return Result.error("获取热门商品失败");
        }
    }

    /**
     * 获取新品商品
     */
    @GetMapping("/new")
    public Result<IPage<Product>> getNewProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            // MyBatis-Plus的Page对象使用从1开始的页码，前端传递从0开始的页码，所以需要+1
            Page<Product> pageable = new Page<>(page + 1, size);
            IPage<Product> products = productService.getNewProducts(pageable);
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取新品商品失败", e);
            return Result.error("获取新品商品失败");
        }
    }

    /**
     * 获取推荐商品
     */
    @GetMapping("/recommended")
    public Result<List<Product>> getRecommendedProducts() {
        try {
            List<Product> products = productService.getRecommendedProducts();
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取推荐商品失败", e);
            return Result.error("获取推荐商品失败");
        }
    }

    /**
     * 获取收藏最多商品
     */
    @GetMapping("/most-favorite")
    public Result<List<Product>> getMostFavoriteProducts() {
        try {
            List<Product> products = productService.getMostFavoriteProducts();
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取收藏最多商品失败", e);
            return Result.error("获取商品失败");
        }
    }

    /**
     * 根据价格范围获取商品
     */
    @GetMapping("/price-range")
    public Result<List<Product>> getProductsByPriceRange(
            @RequestParam @NotNull Double minPrice,
            @RequestParam @NotNull Double maxPrice) {
        try {
            List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("根据价格范围获取商品失败: minPrice={}, maxPrice={}", minPrice, maxPrice, e);
            return Result.error("获取商品失败");
        }
    }

    /**
     * 更新商品浏览时长（用户离开页面时调用）
     */
    @PostMapping("/view/duration")
    public Result<String> updateViewDuration(
            @RequestParam @NotNull Integer userId,
            @RequestParam @NotNull Integer productId,
            @RequestParam @NotNull Integer durationSeconds) {
        try {
            log.info("更新浏览时长请求: userId={}, productId={}, duration={}秒",
                    userId, productId, durationSeconds);

            productViewService.updateLatestViewDuration(userId, productId, durationSeconds);

            return Result.success("更新成功", "OK");
        } catch (Exception e) {
            log.error("更新浏览时长失败: userId={}, productId={}, duration={}",
                    userId, productId, durationSeconds, e);
            return Result.error("更新失败");
        }
    }
}
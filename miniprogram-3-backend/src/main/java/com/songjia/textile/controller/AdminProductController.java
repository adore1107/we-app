package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Product;
import com.songjia.textile.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员商品管理控制器
 * 提供商品的增删改查、批量操作等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductMapper productMapper;

    /**
     * 获取商品列表（管理员版本，支持查询所有状态的商品）
     */
    @GetMapping("/list")
    public Result<IPage<Product>> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        try {
            Page<Product> pageable = new Page<>(page + 1, size);
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("name", keyword.trim());
            }

            // 分类筛选（支持一级和二级分类）
            if (categoryId != null) {
                queryWrapper.and(wrapper ->
                    wrapper.eq("main_category_id", categoryId)
                           .or()
                           .eq("sub_category_id", categoryId)
                );
            }

            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }

            // 排序处理
            if (sortField != null && !sortField.trim().isEmpty()
                    && sortOrder != null && !sortOrder.trim().isEmpty()) {
                // 将驼峰转换为下划线（viewCount -> view_count）
                String dbField = sortField.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

                if ("asc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByAsc(dbField);
                } else if ("desc".equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByDesc(dbField);
                }
            } else {
                // 默认按更新时间降序排列
                queryWrapper.orderByDesc("updated_at", "id");
            }

            IPage<Product> products = productMapper.selectPage(pageable, queryWrapper);

            log.info("管理员获取商品列表: keyword={}, categoryId={}, status={}, sortField={}, sortOrder={}, total={}",
                    keyword, categoryId, status, sortField, sortOrder, products.getTotal());

            return Result.success("获取成功", products);
        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            return Result.error("获取商品列表失败");
        }
    }

    /**
     * 获取商品详情（管理员版本，可查看所有状态的商品）
     */
    @GetMapping("/detail/{id}")
    public Result<Product> getProductDetail(@PathVariable @NotNull Integer id) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            log.info("管理员获取商品详情: id={}", id);
            return Result.success("获取成功", product);
        } catch (Exception e) {
            log.error("获取商品详情失败: id={}", id, e);
            return Result.error("获取商品详情失败");
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    @Transactional
    public Result<Product> addProduct(@Valid @RequestBody Product product) {
        try {
            // 设置默认值
            if (product.getSortOrder() == null) {
                product.setSortOrder(0);
            }
            if (product.getViewCount() == null) {
                product.setViewCount(0);
            }
            if (product.getFavoriteCount() == null) {
                product.setFavoriteCount(0);
            }
            if (product.getIsHot() == null) {
                product.setIsHot(false);
            }
            if (product.getIsNew() == null) {
                product.setIsNew(false);
            }
            if (product.getIsRecommended() == null) {
                product.setIsRecommended(false);
            }
            if (product.getStatus() == null) {
                product.setStatus(true);
            }

            // 插入商品
            int result = productMapper.insert(product);

            if (result > 0) {
                log.info("添加商品成功: id={}, name={}", product.getId(), product.getName());
                return Result.success("添加成功", product);
            } else {
                log.error("添加商品失败: name={}", product.getName());
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            log.error("添加商品异常: name={}", product.getName(), e);
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/update/{id}")
    @Transactional
    public Result<Product> updateProduct(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody Product product) {
        try {
            // 检查商品是否存在
            Product existingProduct = productMapper.selectById(id);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }

            // 设置ID并更新
            product.setId(id);
            // 保留原有的统计数据
            product.setViewCount(existingProduct.getViewCount());
            product.setFavoriteCount(existingProduct.getFavoriteCount());
            // 更新时间由MyBatis-Plus自动填充

            int result = productMapper.updateById(product);

            if (result > 0) {
                // 重新查询返回最新数据
                Product updatedProduct = productMapper.selectById(id);
                log.info("更新商品成功: id={}, name={}", id, product.getName());
                return Result.success("更新成功", updatedProduct);
            } else {
                log.error("更新商品失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品异常: id={}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<String> deleteProduct(@PathVariable @NotNull Integer id) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            int result = productMapper.deleteById(id);

            if (result > 0) {
                log.info("删除商品成功: id={}, name={}", id, product.getName());
                return Result.success("删除成功", "OK");
            } else {
                log.error("删除商品失败: id={}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除商品异常: id={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除商品
     */
    @DeleteMapping("/batch-delete")
    @Transactional
    public Result<String> batchDeleteProducts(@RequestBody List<Integer> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要删除的商品");
            }

            int result = productMapper.deleteBatchIds(ids);

            log.info("批量删除商品: 请求删除{}个, 实际删除{}个", ids.size(), result);

            if (result > 0) {
                return Result.success("删除成功", "成功删除" + result + "个商品");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除商品异常: ids={}", ids, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品状态（上架/下架）
     */
    @PutMapping("/status/{id}")
    @Transactional
    public Result<String> updateProductStatus(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Boolean status) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            product.setStatus(status);
            int result = productMapper.updateById(product);

            if (result > 0) {
                log.info("更新商品状态成功: id={}, status={}", id, status);
                return Result.success(status ? "上架成功" : "下架成功", "OK");
            } else {
                log.error("更新商品状态失败: id={}", id);
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品状态异常: id={}, status={}", id, status, e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新商品状态
     */
    @PutMapping("/batch-status")
    @Transactional
    public Result<String> batchUpdateProductStatus(
            @RequestBody List<Integer> ids,
            @RequestParam @NotNull Boolean status) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("请选择要操作的商品");
            }

            int successCount = 0;
            for (Integer id : ids) {
                Product product = productMapper.selectById(id);
                if (product != null) {
                    product.setStatus(status);
                    int result = productMapper.updateById(product);
                    if (result > 0) {
                        successCount++;
                    }
                }
            }

            log.info("批量更新商品状态: 请求更新{}个, 实际更新{}个, status={}",
                    ids.size(), successCount, status);

            if (successCount > 0) {
                String message = String.format("成功%s%d个商品", status ? "上架" : "下架", successCount);
                return Result.success(message, "OK");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("批量更新商品状态异常: ids={}, status={}", ids, status, e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品标签（热门/最新/推荐）
     */
    @PutMapping("/tags/{id}")
    @Transactional
    public Result<String> updateProductTags(
            @PathVariable @NotNull Integer id,
            @RequestParam(required = false) Boolean isHot,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(required = false) Boolean isRecommended) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            if (isHot != null) {
                product.setIsHot(isHot);
            }
            if (isNew != null) {
                product.setIsNew(isNew);
            }
            if (isRecommended != null) {
                product.setIsRecommended(isRecommended);
            }

            int result = productMapper.updateById(product);

            if (result > 0) {
                log.info("更新商品标签成功: id={}, isHot={}, isNew={}, isRecommended={}",
                        id, isHot, isNew, isRecommended);
                return Result.success("更新成功", "OK");
            } else {
                log.error("更新商品标签失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品标签异常: id={}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品排序权重
     */
    @PutMapping("/sort-order/{id}")
    @Transactional
    public Result<String> updateSortOrder(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Integer sortOrder) {
        try {
            Product product = productMapper.selectById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            product.setSortOrder(sortOrder);
            int result = productMapper.updateById(product);

            if (result > 0) {
                log.info("更新商品排序权重成功: id={}, sortOrder={}", id, sortOrder);
                return Result.success("更新成功", "OK");
            } else {
                log.error("更新商品排序权重失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品排序权重异常: id={}, sortOrder={}", id, sortOrder, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}

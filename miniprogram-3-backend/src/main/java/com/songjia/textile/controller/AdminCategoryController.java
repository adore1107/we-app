package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Category;
import com.songjia.textile.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理员分类管理控制器
 * 提供分类的增删改查等管理功能
 */
@Slf4j
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryMapper categoryMapper;

    /**
     * 获取所有分类（管理员版本，包含禁用的分类）
     */
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        try {
            List<Category> categories = categoryMapper.selectList(null);
            log.info("管理员获取分类列表: total={}", categories.size());
            return Result.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.error("获取分类列表失败");
        }
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/detail/{id}")
    public Result<Category> getCategoryDetail(@PathVariable @NotNull Integer id) {
        try {
            Category category = categoryMapper.selectById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }
            log.info("管理员获取分类详情: id={}", id);
            return Result.success("获取成功", category);
        } catch (Exception e) {
            log.error("获取分类详情失败: id={}", id, e);
            return Result.error("获取分类详情失败");
        }
    }

    /**
     * 添加分类
     */
    @PostMapping("/add")
    @Transactional
    public Result<Category> addCategory(@Valid @RequestBody Category category) {
        try {
            // 设置默认值
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            if (category.getStatus() == null) {
                category.setStatus(true);
            }

            // 根据parent_id自动设置level
            if (category.getParentId() == null) {
                category.setLevel(1); // 一级分类
            } else {
                category.setLevel(2); // 二级分类
            }

            int result = categoryMapper.insert(category);

            if (result > 0) {
                log.info("添加分类成功: id={}, name={}, level={}",
                        category.getId(), category.getName(), category.getLevel());
                return Result.success("添加成功", category);
            } else {
                log.error("添加分类失败: name={}", category.getName());
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            log.error("添加分类异常: name={}", category.getName(), e);
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/update/{id}")
    @Transactional
    public Result<Category> updateCategory(
            @PathVariable @NotNull Integer id,
            @Valid @RequestBody Category category) {
        try {
            // 检查分类是否存在
            Category existingCategory = categoryMapper.selectById(id);
            if (existingCategory == null) {
                return Result.error("分类不存在");
            }

            // 设置ID并更新
            category.setId(id);

            // 根据parent_id自动设置level
            if (category.getParentId() == null) {
                category.setLevel(1); // 一级分类
            } else {
                category.setLevel(2); // 二级分类
            }

            int result = categoryMapper.updateById(category);

            if (result > 0) {
                // 重新查询返回最新数据
                Category updatedCategory = categoryMapper.selectById(id);
                log.info("更新分类成功: id={}, name={}", id, category.getName());
                return Result.success("更新成功", updatedCategory);
            } else {
                log.error("更新分类失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新分类异常: id={}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<String> deleteCategory(@PathVariable @NotNull Integer id) {
        try {
            // 检查分类是否存在
            Category category = categoryMapper.selectById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }

            // 如果是一级分类，检查是否有子分类
            if (category.getLevel() == 1) {
                // 检查是否有子分类（包含所有状态的子分类）
                QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("parent_id", id);
                Long count = categoryMapper.selectCount(queryWrapper);
                if (count > 0) {
                    return Result.error("该分类下还有子分类，无法删除");
                }
            }

            int result = categoryMapper.deleteById(id);

            if (result > 0) {
                log.info("删除分类成功: id={}, name={}", id, category.getName());
                return Result.success("删除成功", "OK");
            } else {
                log.error("删除分类失败: id={}", id);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除分类异常: id={}", id, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类状态（启用/禁用）
     */
    @PutMapping("/status/{id}")
    @Transactional
    public Result<String> updateCategoryStatus(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Boolean status) {
        try {
            Category category = categoryMapper.selectById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }

            category.setStatus(status);
            int result = categoryMapper.updateById(category);

            if (result > 0) {
                log.info("更新分类状态成功: id={}, status={}", id, status);
                return Result.success(status ? "启用成功" : "禁用成功", "OK");
            } else {
                log.error("更新分类状态失败: id={}", id);
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新分类状态异常: id={}, status={}", id, status, e);
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类排序
     */
    @PutMapping("/sort-order/{id}")
    @Transactional
    public Result<String> updateSortOrder(
            @PathVariable @NotNull Integer id,
            @RequestParam @NotNull Integer sortOrder) {
        try {
            Category category = categoryMapper.selectById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }

            category.setSortOrder(sortOrder);
            int result = categoryMapper.updateById(category);

            if (result > 0) {
                log.info("更新分类排序成功: id={}, sortOrder={}", id, sortOrder);
                return Result.success("更新成功", "OK");
            } else {
                log.error("更新分类排序失败: id={}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新分类排序异常: id={}, sortOrder={}", id, sortOrder, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }
}

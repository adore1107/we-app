package com.songjia.textile.controller;

import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Category;
import com.songjia.textile.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            log.info("获取分类列表成功，数量: {}, 第一个分类: {}", categories.size(),
                    categories.isEmpty() ? null : categories.get(0));
            return Result.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.error("获取分类失败");
        }
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    public Result<Category> getCategoryById(@PathVariable Integer id) {
        try {
            return categoryService.getCategoryById(id)
                    .map(category -> Result.success("获取成功", category))
                    .orElse(Result.error("分类不存在"));
        } catch (Exception e) {
            log.error("获取分类详情失败: id={}", id, e);
            return Result.error("获取分类详情失败");
        }
    }

    /**
     * 获取树形分类结构（一级分类+子分类）
     * 推荐前端使用此接口
     */
    @GetMapping("/tree")
    public Result<List<Category>> getCategoryTree() {
        try {
            List<Category> tree = categoryService.getCategoryTree();
            log.info("获取树形分类结构成功，一级分类数量: {}", tree.size());
            return Result.success("获取成功", tree);
        } catch (Exception e) {
            log.error("获取树形分类结构失败", e);
            return Result.error("获取分类失败");
        }
    }

    /**
     * 获取所有一级分类
     */
    @GetMapping("/main")
    public Result<List<Category>> getMainCategories() {
        try {
            List<Category> categories = categoryService.getMainCategories();
            log.info("获取一级分类成功，数量: {}", categories.size());
            return Result.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取一级分类失败", e);
            return Result.error("获取分类失败");
        }
    }

    /**
     * 获取指定父分类下的所有子分类
     */
    @GetMapping("/sub/{parentId}")
    public Result<List<Category>> getSubCategories(@PathVariable Integer parentId) {
        try {
            List<Category> categories = categoryService.getSubCategories(parentId);
            log.info("获取子分类成功，父分类ID: {}, 子分类数量: {}", parentId, categories.size());
            return Result.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取子分类失败: parentId={}", parentId, e);
            return Result.error("获取子分类失败");
        }
    }
}
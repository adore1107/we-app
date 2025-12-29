package com.songjia.textile.service;

import com.songjia.textile.entity.Category;
import com.songjia.textile.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分类服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 获取所有启用的分类
     */
    public List<Category> getAllCategories() {
        List<Category> categories = categoryMapper.findByStatusOrderBySortOrderAsc(true);
        log.info("获取到 {} 个启用的分类", categories.size());
        return categories;
    }

    /**
     * 根据ID获取分类
     */
    public Optional<Category> getCategoryById(Integer id) {
        return Optional.ofNullable(categoryMapper.findById(id));
    }

    /**
     * 添加新分类
     */
    @Transactional
    public Category addCategory(Category category) {
        int result = categoryMapper.insert(category);
        if (result > 0) {
            log.info("成功添加分类: {}", category.getName());
            return category;
        } else {
            log.error("添加分类失败: {}", category.getName());
            throw new RuntimeException("添加分类失败");
        }
    }

    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Category category) {
        int result = categoryMapper.updateById(category);
        if (result > 0) {
            log.info("成功更新分类: {}", category.getName());
            return category;
        } else {
            log.error("更新分类失败: {}", category.getName());
            throw new RuntimeException("更新分类失败");
        }
    }

    /**
     * 删除分类（逻辑删除）
     */
    @Transactional
    public boolean deleteCategory(Integer id) {
        int result = categoryMapper.deleteById(id);
        if (result > 0) {
            log.info("成功删除分类: id={}", id);
            return true;
        } else {
            log.error("删除分类失败: id={}", id);
            return false;
        }
    }

    /**
     * 根据名称查找分类
     */
    public Optional<Category> getCategoryByName(String name) {
        Category category = categoryMapper.findByNameAndStatus(name, true);
        return Optional.ofNullable(category);
    }

    /**
     * 获取所有一级分类
     */
    public List<Category> getMainCategories() {
        List<Category> categories = categoryMapper.findByLevelAndStatusOrderBySortOrderAsc(1, true);
        log.info("获取到 {} 个一级分类", categories.size());
        return categories;
    }

    /**
     * 获取指定父分类下的所有子分类
     */
    public List<Category> getSubCategories(Integer parentId) {
        List<Category> categories = categoryMapper.findByParentIdAndStatusOrderBySortOrderAsc(parentId, true);
        log.info("获取到父分类 {} 下的 {} 个子分类", parentId, categories.size());
        return categories;
    }

    /**
     * 获取树形分类结构（一级分类+子分类）
     */
    public List<Category> getCategoryTree() {
        // 1. 获取所有启用的分类
        List<Category> allCategories = categoryMapper.findByStatusOrderBySortOrderAsc(true);

        // 2. 分离一级分类和二级分类
        List<Category> mainCategories = allCategories.stream()
                .filter(c -> c.getLevel() == 1)
                .collect(Collectors.toList());

        List<Category> subCategories = allCategories.stream()
                .filter(c -> c.getLevel() == 2)
                .collect(Collectors.toList());

        // 3. 为每个一级分类添加其子分类
        for (Category mainCategory : mainCategories) {
            List<Category> children = subCategories.stream()
                    .filter(sub -> sub.getParentId() != null && sub.getParentId().equals(mainCategory.getId()))
                    .collect(Collectors.toList());
            mainCategory.setChildren(children);
        }

        log.info("获取到树形分类结构: {} 个一级分类", mainCategories.size());
        return mainCategories;
    }

    /**
     * 获取指定一级分类的树形结构
     */
    public Category getCategoryTreeById(Integer mainCategoryId) {
        Category mainCategory = categoryMapper.findById(mainCategoryId);
        if (mainCategory == null || mainCategory.getLevel() != 1) {
            return null;
        }

        List<Category> children = getSubCategories(mainCategoryId);
        mainCategory.setChildren(children);

        return mainCategory;
    }
}
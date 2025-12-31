package com.songjia.textile.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.dto.ProductSearchDTO;
import com.songjia.textile.entity.Product;
import com.songjia.textile.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductMapper productMapper;

    /**
     * 分页获取所有商品（支持排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> getAllProducts(int page, int size, String sortType) {
        Page<Product> pageable = new Page<>(page + 1, size);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", true);

        // 根据sortType动态排序
        applySorting(queryWrapper, sortType);

        return productMapper.selectPage(pageable, queryWrapper);
    }

    /**
     * 分页获取所有商品（兼容旧版本，默认综合排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> getAllProducts(int page, int size) {
        return getAllProducts(page, size, "overall");
    }

    /**
     * 根据分类获取商品
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Integer categoryId) {
        if (categoryId == null) {
            // 如果categoryId为null，返回所有商品
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", true);
            // 移除deleted字段查询，数据库表没有该字段
            queryWrapper.orderByDesc("sort_order", "id");
            return productMapper.selectList(queryWrapper);
        }
        return productMapper.findByCategoryIdAndStatusOrderBySortOrderDesc(categoryId, true);
    }

    /**
     * 根据分类分页获取商品（支持排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> getProductsByCategory(Integer categoryId, int page, int size, String sortType) {
        log.info("=== ProductService.getProductsByCategory 开始 ===");
        log.info("Service参数 - categoryId: {}, page: {}, size: {}, sortType: {}", categoryId, page, size, sortType);

        // MyBatis-Plus的Page对象使用从1开始的页码，前端传递从0开始的页码，所以需要+1
        Page<Product> pageable = new Page<>(page + 1, size);
        log.info("创建的Page对象 - 前端页码:{}, MyBatis-Plus current:{}, size:{}", page, pageable.getCurrent(), pageable.getSize());

        if (categoryId == null) {
            // 如果categoryId为null，返回所有商品
            log.info("categoryId为null，查询所有商品");
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", true);
            applySorting(queryWrapper, sortType);
            IPage<Product> result = productMapper.selectPage(pageable, queryWrapper);
            log.info("QueryWrapper查询结果 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());
            return result;
        }

        log.info("使用QueryWrapper自动分页查询分类商品");
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        // 支持一级分类和二级分类查询
        queryWrapper.and(wrapper -> wrapper.eq("main_category_id", categoryId).or().eq("sub_category_id", categoryId));
        queryWrapper.eq("status", true);
        applySorting(queryWrapper, sortType);

        IPage<Product> result = productMapper.selectPage(pageable, queryWrapper);
        log.info("QueryWrapper分页查询结果 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 根据分类分页获取商品（兼容旧版本，默认综合排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> getProductsByCategory(Integer categoryId, int page, int size) {
        return getProductsByCategory(categoryId, page, size, "overall");
    }

    /**
     * 获取商品详情
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Integer id) {
        log.info("获取商品详情: id={}", id);
        Product product = productMapper.selectById(id);
        return Optional.ofNullable(product);
    }

    /**
     * 增加商品浏览次数
     */
    public void incrementViewCount(Integer productId) {
        productMapper.incrementViewCount(productId);
        log.info("增加商品浏览次数: productId={}", productId);
    }

    /**
     * 获取热门商品
     */
    @Transactional(readOnly = true)
    public List<Product> getHotProducts() {
        List<Product> hotProducts = productMapper.findByIsHotAndStatusOrderBySortOrderDesc(true, true);
        log.info("找到 {} 个热门商品", hotProducts.size());
        return hotProducts;
    }

    /**
     * 获取热门商品（分页）
     */
    @Transactional(readOnly = true)
    public IPage<Product> getHotProducts(Page<Product> pageable) {
        IPage<Product> hotProducts = productMapper.findByIsHotAndStatusOrderBySortOrderDesc(pageable, true, true);
        log.info("分页获取 {} 个热门商品", hotProducts.getRecords().size());
        return hotProducts;
    }

    /**
     * 获取新品商品
     */
    @Transactional(readOnly = true)
    public List<Product> getNewProducts() {
        return productMapper.findByIsNewAndStatusOrderBySortOrderDesc(true, true);
    }

    /**
     * 获取新品商品（分页）
     */
    @Transactional(readOnly = true)
    public IPage<Product> getNewProducts(Page<Product> pageable) {
        return productMapper.findByIsNewAndStatusOrderBySortOrderDesc(pageable, true, true);
    }

    /**
     * 获取推荐商品
     */
    @Transactional(readOnly = true)
    public List<Product> getRecommendedProducts() {
        return productMapper.findByIsRecommendedAndStatusOrderBySortOrderDesc(true, true);
    }

    /**
     * 搜索商品
     */
    @Transactional(readOnly = true)
    public IPage<Product> searchProducts(String keyword, int page, int size) {
        Page<Product> pageable = new Page<>(page + 1, size);
        return productMapper.findByNameContainingOrDescriptionContaining(pageable, keyword, true);
    }

    /**
     * 根据商品名称模糊搜索 - 用于前端搜索功能（支持排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> searchProductsByName(String keyword, int pageNum, int pageSize, String sortType) {
        log.info("全局搜索商品: keyword={}, page={}, size={}, sortType={}", keyword, pageNum, pageSize, sortType);

        // MyBatis-Plus的Page对象使用从1开始的页码，前端传递从0开始的页码，所以需要+1
        Page<Product> pageable = new Page<>(pageNum + 1, pageSize);

        // 使用QueryWrapper构建查询条件
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", true);
        queryWrapper.like("name", keyword);
        applySorting(queryWrapper, sortType);

        IPage<Product> result = productMapper.selectPage(pageable, queryWrapper);
        log.info("全局搜索结果 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 根据商品名称模糊搜索（兼容旧版本，默认综合排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> searchProductsByName(String keyword, int pageNum, int pageSize) {
        return searchProductsByName(keyword, pageNum, pageSize, "overall");
    }

    /**
     * 在指定分类中搜索商品 - 支持分类内关键词搜索（支持排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> searchProductsInCategory(Integer categoryId, String keyword, int pageNum, int pageSize, String sortType) {
        log.info("分类内搜索商品: categoryId={}, keyword={}, page={}, size={}, sortType={}", categoryId, keyword, pageNum, pageSize, sortType);

        // MyBatis-Plus的Page对象使用从1开始的页码，前端传递从0开始的页码，所以需要+1
        Page<Product> pageable = new Page<>(pageNum + 1, pageSize);

        // 使用QueryWrapper构建查询条件
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        // 支持一级分类和二级分类查询
        queryWrapper.and(wrapper -> wrapper.eq("main_category_id", categoryId).or().eq("sub_category_id", categoryId));
        queryWrapper.eq("status", true);  // 只查询上架商品
        queryWrapper.like("name", keyword);  // 模糊搜索商品名称
        applySorting(queryWrapper, sortType);  // 应用动态排序

        IPage<Product> result = productMapper.selectPage(pageable, queryWrapper);
        log.info("分类内搜索结果 - 总记录数: {}, 当前页记录数: {}", result.getTotal(), result.getRecords().size());

        return result;
    }

    /**
     * 在指定分类中搜索商品（兼容旧版本，默认综合排序）
     */
    @Transactional(readOnly = true)
    public IPage<Product> searchProductsInCategory(Integer categoryId, String keyword, int pageNum, int pageSize) {
        return searchProductsInCategory(categoryId, keyword, pageNum, pageSize, "overall");
    }

    /**
     * 更新商品收藏次数
     */
    public void updateFavoriteCount(Integer productId, int count) {
        productMapper.updateFavoriteCount(productId, count);
        log.info("更新商品收藏次数: productId={}, count={}", productId, count);
    }

    /**
     * 获取收藏最多的商品
     */
    @Transactional(readOnly = true)
    public List<Product> getMostFavoriteProducts() {
        List<Product> products = productMapper.findTop10ByStatusOrderByFavoriteCountDesc(true);
        log.info("找到 {} 个收藏最多的商品", products.size());
        return products;
    }

    /**
     * 获取浏览最多的商品
     */
    @Transactional(readOnly = true)
    public List<Product> getMostViewedProducts() {
        List<Product> products = productMapper.findTop10ByStatusOrderByViewCountDesc(true);
        log.info("找到 {} 个浏览最多的商品", products.size());
        return products;
    }

    /**
     * 根据价格范围查找商品
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        List<Product> products = productMapper.findByPriceRange(minPrice, maxPrice, true);
        log.info("在价格范围 {} - {} 之间找到 {} 个商品", minPrice, maxPrice, products.size());
        return products;
    }

    /**
     * 获取分类商品数量统计
     */
    @Transactional(readOnly = true)
    public long getCategoryProductCount(Integer categoryId) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", true);

        if (categoryId != null) {
            // 支持一级分类和二级分类查询
            queryWrapper.and(wrapper -> wrapper.eq("main_category_id", categoryId).or().eq("sub_category_id", categoryId));
        }

        return productMapper.selectCount(queryWrapper);
    }

    /**
     * 应用动态排序逻辑
     * @param queryWrapper 查询条件构造器
     * @param sortType 排序类型：overall=综合, latest=最新, hot=热门
     */
    private void applySorting(QueryWrapper<Product> queryWrapper, String sortType) {
        if (sortType == null || sortType.trim().isEmpty()) {
            sortType = "overall";
        }

        switch (sortType.toLowerCase()) {
            case "latest":
                // 最新：按创建时间降序
                queryWrapper.orderByDesc("created_at", "id");
                log.debug("应用排序规则: 最新 (created_at DESC)");
                break;
            case "hot":
                // 热门：按浏览次数降序，再按收藏次数降序
                queryWrapper.orderByDesc("view_count", "favorite_count", "id");
                log.debug("应用排序规则: 热门 (view_count DESC, favorite_count DESC)");
                break;
            case "overall":
            default:
                // 综合：按推荐排序字段降序（默认）
                queryWrapper.orderByDesc("sort_order", "id");
                log.debug("应用排序规则: 综合 (sort_order DESC)");
                break;
        }
    }

    /**
     * 获取分类下的热门商品
     */
    @Transactional(readOnly = true)
    public IPage<Product> getCategoryHotProducts(Integer categoryId, int page, int size) {
        Page<Product> pageable = new Page<>(page + 1, size);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", true);
        queryWrapper.eq("is_hot", true);

        if (categoryId != null) {
            // 支持一级分类和二级分类查询
            queryWrapper.and(wrapper -> wrapper.eq("main_category_id", categoryId).or().eq("sub_category_id", categoryId));
        }

        queryWrapper.orderByDesc("sort_order", "id");
        return productMapper.selectPage(pageable, queryWrapper);
    }

    /**
     * 获取分类下的新品商品
     */
    @Transactional(readOnly = true)
    public IPage<Product> getCategoryNewProducts(Integer categoryId, int page, int size) {
        Page<Product> pageable = new Page<>(page + 1, size);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", true);
        queryWrapper.eq("is_new", true);

        if (categoryId != null) {
            // 支持一级分类和二级分类查询
            queryWrapper.and(wrapper -> wrapper.eq("main_category_id", categoryId).or().eq("sub_category_id", categoryId));
        }

        queryWrapper.orderByDesc("sort_order", "id");
        return productMapper.selectPage(pageable, queryWrapper);
    }
}
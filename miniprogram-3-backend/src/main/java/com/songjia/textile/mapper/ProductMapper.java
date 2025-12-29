package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.dto.ProductSearchDTO;
import com.songjia.textile.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据分类查找商品
     */
    List<Product> findByCategoryIdAndStatusOrderBySortOrderDesc(@Param("categoryId") Integer categoryId, @Param("status") Boolean status);

    /**
     * 分页根据分类查找商品
     */
    IPage<Product> findByCategoryIdAndStatusOrderBySortOrderDesc(
            Page<Product> page,
            @Param("categoryId") Integer categoryId,
            @Param("status") Boolean status);

    /**
     * 查找热门商品
     */
    List<Product> findByIsHotAndStatusOrderBySortOrderDesc(@Param("isHot") Boolean isHot, @Param("status") Boolean status);

    /**
     * 分页查找热门商品
     */
    IPage<Product> findByIsHotAndStatusOrderBySortOrderDesc(
            Page<Product> page,
            @Param("isHot") Boolean isHot,
            @Param("status") Boolean status);

    /**
     * 查找新品商品
     */
    List<Product> findByIsNewAndStatusOrderBySortOrderDesc(@Param("isNew") Boolean isNew, @Param("status") Boolean status);

    /**
     * 分页查找新品商品
     */
    IPage<Product> findByIsNewAndStatusOrderBySortOrderDesc(
            Page<Product> page,
            @Param("isNew") Boolean isNew,
            @Param("status") Boolean status);

    /**
     * 查找推荐商品
     */
    List<Product> findByIsRecommendedAndStatusOrderBySortOrderDesc(@Param("isRecommended") Boolean isRecommended, @Param("status") Boolean status);

    /**
     * 分页查找所有上架商品
     */
    IPage<Product> findByStatusOrderBySortOrderDesc(@Param("status") Boolean status, Page<Product> page);

    /**
     * 查找所有上架商品（非分页）
     */
    List<Product> findByStatusOrderBySortOrderDesc(@Param("status") Boolean status);

    /**
     * 搜索商品（根据名称和描述）
     */
    IPage<Product> findByNameContainingOrDescriptionContaining(
            Page<Product> page,
            @Param("keyword") String keyword,
            @Param("status") Boolean status);

    /**
     * 根据价格范围查找商品
     */
    List<Product> findByPriceRange(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("status") Boolean status);

    /**
     * 增加浏览次数
     */
    int incrementViewCount(@Param("productId") Integer productId);

    /**
     * 更新收藏次数
     */
    int updateFavoriteCount(@Param("productId") Integer productId, @Param("count") Integer count);

    /**
     * 查找收藏数量最多的商品
     */
    List<Product> findTop10ByStatusOrderByFavoriteCountDesc(@Param("status") Boolean status);

    /**
     * 查找浏览次数最多的商品
     */
    List<Product> findTop10ByStatusOrderByViewCountDesc(@Param("status") Boolean status);

    /**
     * 根据ID查找商品
     */
    Product findById(@Param("id") Integer id);

    /**
     * 根据商品名称模糊搜索 - 用于前端搜索功能
     */
    IPage<Product> searchProductsByName(
            Page<Product> page,
            @Param("keyword") String keyword,
            @Param("status") Boolean status);
}
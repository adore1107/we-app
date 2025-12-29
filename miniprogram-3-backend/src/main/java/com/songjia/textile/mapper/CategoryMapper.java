package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类数据访问接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据状态查找分类
     */
    List<Category> findByStatusOrderBySortOrderAsc(@Param("status") Boolean status);

    /**
     * 分页根据状态查找分类
     */
    IPage<Category> findByStatusOrderBySortOrderAsc(@Param("status") Boolean status, Page<Category> page);

    /**
     * 根据名称查找分类
     */
    Category findByNameAndStatus(@Param("name") String name, @Param("status") Boolean status);

    /**
     * 根据ID查找分类
     */
    Category findById(@Param("id") Integer id);

    /**
     * 根据层级和状态查找分类
     */
    List<Category> findByLevelAndStatusOrderBySortOrderAsc(@Param("level") Integer level, @Param("status") Boolean status);

    /**
     * 根据父分类ID和状态查找分类
     */
    List<Category> findByParentIdAndStatusOrderBySortOrderAsc(@Param("parentId") Integer parentId, @Param("status") Boolean status);
}
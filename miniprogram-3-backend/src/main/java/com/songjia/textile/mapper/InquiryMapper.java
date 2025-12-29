package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.Inquiry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 询价数据访问接口
 */
@Mapper
public interface InquiryMapper extends BaseMapper<Inquiry> {

    /**
     * 根据用户ID查找询价记录
     */
    List<Inquiry> findByUserId(@Param("userId") Integer userId);

    /**
     * 分页根据用户ID查找询价记录
     */
    IPage<Inquiry> findByUserId(@Param("userId") Integer userId, Page<Inquiry> page);

    /**
     * 根据状态查找询价记录
     */
    List<Inquiry> findByStatus(@Param("status") Integer status);

    /**
     * 分页根据状态查找询价记录
     */
    IPage<Inquiry> findByStatus(@Param("status") Integer status, Page<Inquiry> page);

    /**
     * 统计询价数量
     */
    int countByStatus(@Param("status") Integer status);

    /**
     * 统计用户询价数量
     */
    int countByUserId(@Param("userId") Integer userId);
}
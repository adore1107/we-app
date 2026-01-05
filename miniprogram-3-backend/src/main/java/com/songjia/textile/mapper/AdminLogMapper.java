package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.songjia.textile.entity.AdminLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志Mapper接口
 */
@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLog> {
    // 继承BaseMapper即可，基础CRUD方法已包含
}

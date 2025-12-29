package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.songjia.textile.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据OpenID查找用户
     */
    User findByOpenid(@Param("openid") String openid);

    /**
     * 根据ID查找用户（包含逻辑删除检查）
     */
    User findByIdAndDeleted(@Param("id") Integer id);

    /**
     * 分页获取用户列表
     */
    IPage<User> findAllUsers(Page<User> page);

    /**
     * 根据手机号查找用户
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 更新最后登录时间
     */
    int updateLastLoginTime(@Param("id") Integer id);
}
package com.songjia.textile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.songjia.textile.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户名查找管理员
     */
    @Select("SELECT * FROM admins WHERE username = #{username}")
    Optional<Admin> findByUsername(@Param("username") String username);

    /**
     * 根据用户名查找启用状态的管理员
     */
    @Select("SELECT * FROM admins WHERE username = #{username} AND status = 1")
    Optional<Admin> findByUsernameAndStatus(@Param("username") String username);
}

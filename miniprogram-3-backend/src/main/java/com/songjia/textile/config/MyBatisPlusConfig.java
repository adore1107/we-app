package com.songjia.textile.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件 - 添加更多配置
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 最大单页限制数量
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        paginationInterceptor.setOptimizeJoin(true); // 优化join查询

        interceptor.addInnerInterceptor(paginationInterceptor);

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 创建时间
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                // 更新时间
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());

                // 设置默认值
                this.strictInsertFill(metaObject, "status", Boolean.class, true);
                this.strictInsertFill(metaObject, "minOrderQuantity", Integer.class, 1);
                this.strictInsertFill(metaObject, "unit", String.class, "件");
                this.strictInsertFill(metaObject, "leadTime", Integer.class, 7);
                this.strictInsertFill(metaObject, "sortOrder", Integer.class, 0);
                this.strictInsertFill(metaObject, "isHot", Boolean.class, false);
                this.strictInsertFill(metaObject, "isNew", Boolean.class, false);
                this.strictInsertFill(metaObject, "isRecommended", Boolean.class, false);
                this.strictInsertFill(metaObject, "viewCount", Integer.class, 0);
                this.strictInsertFill(metaObject, "favoriteCount", Integer.class, 0);
                // this.strictInsertFill(metaObject, "deleted", Integer.class, 0);  // 数据库表没有deleted字段，注释掉

                // Category 默认值
                if (metaObject.getOriginalObject() instanceof com.songjia.textile.entity.Category) {
                    this.strictInsertFill(metaObject, "sortOrder", Integer.class, 0);
                }

                // Inquiry 默认值
                if (metaObject.getOriginalObject() instanceof com.songjia.textile.entity.Inquiry) {
                    this.strictInsertFill(metaObject, "status", Integer.class, 0);
                }

                // Banner 默认值
                if (metaObject.getOriginalObject() instanceof com.songjia.textile.entity.Banner) {
                    this.strictInsertFill(metaObject, "status", Boolean.class, true);
                    this.strictInsertFill(metaObject, "sortOrder", Integer.class, 0);
                    this.strictInsertFill(metaObject, "linkType", String.class, "none");
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时间
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
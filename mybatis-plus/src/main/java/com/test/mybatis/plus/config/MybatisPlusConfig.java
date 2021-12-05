package com.test.mybatis.plus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;

/**
 * Mybatis Plus配置类
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {

    /**
     * 添加动态表名和乐观锁拦截器
     *
     * @return Mybatis Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        // 动态修改表名
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) ->
                JobTableHelper.TABLE_NAME.equals(tableName) ? JobTableHelper.getRealTableName() : tableName);
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }
}

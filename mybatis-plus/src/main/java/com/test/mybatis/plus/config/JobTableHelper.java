package com.test.mybatis.plus.config;

import java.util.HashMap;
import java.util.Map;

import com.test.mybatis.plus.exception.BizException;
import com.test.mybatis.plus.utils.StringUtils;

/**
 * Job helper
 */
public class JobTableHelper {
    private static final ThreadLocal<Map<String, Object>> JOB_DATA = ThreadLocal.withInitial(HashMap::new);

    /**
     * 任务表名
     */
    public static final String TABLE_NAME = "job";
    public static final String KEY_JOB_NAME_PREFIX = "_job_pre_";

    public static final String KEY_JOB_NAME_SUFFIX = "_job_suf_";


    /**
     * 设置表名前缀
     *
     * @param prefix 前缀
     */
    public static void setTableNamePrefix(String prefix) {
        JOB_DATA.get().put(KEY_JOB_NAME_PREFIX, prefix);
    }

    /**
     * 设置表名后缀
     *
     * @param suffix 后缀
     */
    public static void setTableNameSuffix(String suffix) {
        JOB_DATA.get().put(KEY_JOB_NAME_SUFFIX, suffix);
    }

    static String getRealTableName() {
        Map<String, Object> map = JOB_DATA.get();
        String prefix = (String) map.get(KEY_JOB_NAME_PREFIX);
        if (StringUtils.isEmpty(prefix)) {
            throw new BizException("please set job table name!");
        }
        String suffix = (String) map.get(KEY_JOB_NAME_SUFFIX);
        String tableName = prefix + TABLE_NAME;
        return StringUtils.isEmpty(suffix) ? tableName : tableName + suffix;
    }
}

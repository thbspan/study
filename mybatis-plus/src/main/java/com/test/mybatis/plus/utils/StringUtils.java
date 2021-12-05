package com.test.mybatis.plus.utils;

public final class StringUtils {
    private StringUtils() {
        super();
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}

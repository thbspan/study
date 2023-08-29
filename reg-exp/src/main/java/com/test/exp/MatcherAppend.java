package com.test.exp;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用正则表达式进行连续替换
 */
public class MatcherAppend {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(\\w+)}");

    private final Map<String, String> values;

    public MatcherAppend(Map<String, String> values) {
        this.values = values;
    }

    public String replaceAll(String expression) {
        StringBuffer sb = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);

        while (matcher.find()) {
            String key = matcher.group(1);
            String value = values.get(key);
            if (value == null) {
                value = '|' + key + '|';
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

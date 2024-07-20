package com.test.zookeeper.config;

import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * 参考{@link RandomValuePropertySource}，做一些修改，值随机一次后固定
 */
public class RandomOnceValuePropertySource extends PropertySource<RandomOnce> {
    public static final String RANDOM_ONCE_PROPERTY_SOURCE_NAME = "randomOnce";
    private static final String PREFIX = RANDOM_ONCE_PROPERTY_SOURCE_NAME + '.';

    public RandomOnceValuePropertySource(String name) {
        super(name, new RandomOnce());
    }

    public RandomOnceValuePropertySource() {
        this(RANDOM_ONCE_PROPERTY_SOURCE_NAME);
    }

    @Override
    public Object getProperty(String name) {
        if (!name.startsWith(PREFIX)) {
            return null;
        }
        return getRandomOnceValue(name.substring(PREFIX.length()));
    }

    private Object getRandomOnceValue(String type) {
        String range = getRange(type, "value");

        if (range != null) {
            return getNextValueInRange(range);
        }
        return null;
    }

    private int getNextValueInRange(String range) {
        String[] tokens = StringUtils.commaDelimitedListToStringArray(range);
        int start = Integer.parseInt(tokens[0]);
        if (tokens.length == 1) {
            return getSource().nextValue(start);
        }
        return getSource().nextValue(start, Integer.parseInt(tokens[1]));
    }

    private String getRange(String type, String prefix) {
        if (type.startsWith(prefix)) {
            int startIndex = prefix.length() + 1;
            if (type.length() > startIndex) {
                return type.substring(startIndex, type.length() - 1);
            }
        }
        return null;
    }
}

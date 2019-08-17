package com.test.exp;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MatcherAppendTest {

    @Test
    public void testReplaceAll() {
        Map<String, String> values = new HashMap<>();
        values.put("cat", "dog");
        MatcherAppend matcherAppend = new MatcherAppend(values);

        System.out.println(matcherAppend.replaceAll("one ${cat} two ${cat}s in the yard"));
    }
}

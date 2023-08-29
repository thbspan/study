package com.test.exp;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MatcherAppendTest {

    @Test
    public void testReplaceAll() {
        Map<String, String> values = new HashMap<>();
        values.put("cat", "dog");
        MatcherAppend matcherAppend = new MatcherAppend(values);

        Assertions.assertEquals(matcherAppend.replaceAll("one ${cat} two ${cat}s in the yard"),
                "one dog two dogs in the yard");
    }
}

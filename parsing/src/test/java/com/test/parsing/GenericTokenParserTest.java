package com.test.parsing;

import org.junit.jupiter.api.Test;

public class GenericTokenParserTest {

    @Test
    public void test() {
        GenericTokenParser parser = new GenericTokenParser("${", "}", content -> new StringBuilder(content).reverse().toString());
        System.out.println(parser.parseWithEscapeChar("\\${sdfs"));
        System.out.println(parser.parseWithEscapeChar("${sdfs"));
        System.out.println(parser.parseWithEscapeChar("${sdfs}-${cd}"));
    }
}

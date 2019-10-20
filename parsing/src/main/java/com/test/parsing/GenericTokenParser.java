package com.test.parsing;

public class GenericTokenParser {
    private final String openToken;
    private final String closeToken;
    private final TokenHandler handler;
    private static final char CHAR_ESCAPSE = '\\';

    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    public String parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        int start = text.indexOf(openToken);
        if (start == -1) {
            return text;
        }
        char[] src = text.toCharArray();
        // 已经处理到的字符索引位置
        int offset = 0;

        StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;

        do {
            if (expression == null) {
                expression = new StringBuilder();
            } else {
                expression.setLength(0);
            }

            builder.append(src, offset, start - offset);
            offset = start + openToken.length();
            int end = text.indexOf(closeToken, offset);
            if (end > -1) {
                expression.append(src, offset, end - offset);
                builder.append(handler.handleToken(expression.toString()));
                offset = end + closeToken.length();
            } else {
                // 没有找到end
                builder.append(src, start, src.length - start);
            }
            start = text.indexOf(openToken, offset);
        } while (start > -1);

        return builder.toString();
    }

    public String parseWithEscapeChar(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int start = text.indexOf(openToken);
        if (start == -1) {
            return text;
        }

        char[] src = text.toCharArray();
        int offset = 0;
        StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        do {
            if (start > 0 && src[start - 1] == CHAR_ESCAPSE) {
                // 这个openToken前面有转义字符
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // 这个openToken前面没有转义字符
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();

                // closeToken index
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == CHAR_ESCAPSE) {
                        // 这个结束符号需要转义
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // 没有找到结束符号
                    builder.append(src, start, src.length -start);
                    offset = src.length;
                } else {
                    builder.append(handler.handleToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        } while (start > -1);
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}

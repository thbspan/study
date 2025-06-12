package com.test.exp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedDecoder {
    // 变量定义映射
    private final Map<String, String> rawDefs = new LinkedHashMap<>();
    private final Map<String, String> resolved = new HashMap<>();

    // 用于检测循环引用
    private final Set<String> visiting = new HashSet<>();

    // 正则匹配变量引用，如 $word$
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$(\\w+)\\$");

    public String parse(String input) {
        parseAllDefinitions(input);

        // 解码 result 或 res 变量
        return resolved.computeIfAbsent("result", k -> {
            if (rawDefs.containsKey("result")) {
                return decodeWithReferences("result");
            } else {
                return decodeWithReferences("res");
            }
        });
    }

    // 解析所有变量定义，例如 word=2[ab]
    private void parseAllDefinitions(String input) {
        int i = 0;
        while (i < input.length()) {
            int eqIdx = input.indexOf('=', i);
            if (eqIdx == -1) {
                break;
            }
            String key = input.substring(i, eqIdx);
            i = eqIdx + 1;

            int start = i;
            int bracket = 0;
            while (i < input.length()) {
                char ch = input.charAt(i++);
                if (ch == '[') {
                    bracket++;
                } else if (ch == ']') {
                    bracket--;
                    if (bracket == 0) {
                        break;
                    }
                }
            }
            String val = input.substring(start, i);
            rawDefs.put(key, val);
        }
    }

    // 递归解码某个变量，含引用处理和嵌套展开
    private String decodeWithReferences(String key) {
        if (resolved.containsKey(key)) {
            return resolved.get(key);
        }
        if (!rawDefs.containsKey(key)) {
            throw new IllegalArgumentException("Undefined variable: " + key);
        }

        if (visiting.contains(key)) {
            throw new IllegalStateException("Cycle detected in variable: " + key);
        }
        visiting.add(key);
        String raw = rawDefs.get(key);
        // 先解析内部引用
        String replaced = replaceReferences(raw);
        // 再展开嵌套结构，如 3[abc]
        String decoded = decodeString(replaced);

        visiting.remove(key);
        resolved.put(key, decoded);
        return decoded;
    }

    // 替换 $var$ 为其展开值
    private String replaceReferences(String input) {
        Matcher matcher = VAR_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String val = decodeWithReferences(varName);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // 解码形如 3[a2[b]] 的嵌套字符串
    private String decodeString(String s) {
        Deque<Integer> countStack = new ArrayDeque<>();
        Deque<StringBuilder> resultStack = new ArrayDeque<>();
        StringBuilder current = new StringBuilder();
        int num = 0;

        for (char ch : s.toCharArray()) {
            if (Character.isDigit(ch)) {
                num = num * 10 + (ch - '0');
            } else if (ch == '[') {
                countStack.push(num);
                resultStack.push(current);
                current = new StringBuilder();
                num = 0;
            } else if (ch == ']') {
                StringBuilder prev = resultStack.pop();
                int times = countStack.pop();
                for (int i = 0; i < times; i++) {
                    prev.append(current);
                }
                current = prev;
            } else {
                current.append(ch);
            }
        }
        return current.toString();
    }

    // 测试
    public static void main(String[] args) {
        NamedDecoder decoder = new NamedDecoder();

        String input = "word=2[ab]text=3[wordx]result=1[$text$ok]";
        System.out.println("Output: " + decoder.parse(input)); // 应输出：wordxwordxwordxok

        decoder = new NamedDecoder();
        String input2 = "a=2[x]ab=3[y]b=2[a$ab$]res=1[$b$]";
        System.out.println("Output: " + decoder.parse(input2)); // 应输出：xxyyxyy
    }
}

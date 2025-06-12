package com.test.exp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructParser {

    private final Pattern varPattern = Pattern.compile("\\$(\\w+)\\$");

    public Map<String, Object> parse(String input) {
        return parseBlock(input.trim(), new HashMap<>());
    }

    private Map<String, Object> parseBlock(String input, Map<String, Object> parentContext) {
        Map<String, Object> context = new HashMap<>(parentContext);
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) != '[') {
                i++;
                continue;
            }
            int start = i;
            int bracket = 0;
            while (i < input.length()) {
                char ch = input.charAt(i);
                if (ch == '[') {
                    bracket++;
                } else if (ch == ']') {
                    bracket--;
                }
                i++;
                if (bracket == 0) {
                    break;
                }
            }
            String segment = input.substring(start, i);
            parseItem(segment, context);
        }

        return context;
    }

    private void parseItem(String segment, Map<String, Object> context) {
        Matcher matcher = Pattern.compile("\\[(\\w+),\\s*(\\w+),\\s*(.+)]").matcher(segment);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid item: " + segment);

        String key = matcher.group(1);
        String type = matcher.group(2);
        String rawValue = matcher.group(3).trim();

        switch (type) {
            case "int":
                context.put(key, Integer.parseInt(rawValue));
                break;
            case "string":
                context.put(key, resolveVars(rawValue, context));
                break;
            case "compose":
                Map<String, Object> nested = parseBlock(rawValue, context);
                context.put(key, nested);
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private String resolveVars(String s, Map<String, Object> context) {
        Matcher matcher = varPattern.matcher(s);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String var = matcher.group(1);
            Object val = context.getOrDefault(var, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(val.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // 打印 Map 结构（调试用）
    public static void print(Map<String, Object> map, String indent) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            System.out.print(indent + e.getKey() + ": ");
            if (e.getValue() instanceof Map) {
                System.out.println();
                print((Map<String, Object>) e.getValue(), indent + "  ");
            } else {
                System.out.println(e.getValue());
            }
        }
    }

    public static void main(String[] args) {
        String input = "[a, int, 2][b, string, ok][c, compose, [d, int, 1][f, string, $b$]]";

        StructParser parser = new StructParser();
        Map<String, Object> result = parser.parse(input);

        System.out.println("Parsed structure:");
        print(result, "");
    }
}

package com.test.exp.dsl;

import java.util.ArrayList;
import java.util.List;

/**
 * dsl
 * [a: int = 2],
 * [b: string = "ok"],
 * [c: list[int] = [1, 2, 3]],
 * [d: compose = {
 * x: int = 1,
 * y: string = $b$
 * }],
 * [e: int = $a$ + len($c$)]
 */
public class Lexer {
    private final String input;
    private int pos = 0;
    private final List<Token> tokens = new ArrayList<>();

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        while (pos < input.length()) {
            char ch = input.charAt(pos);

            if (Character.isWhitespace(ch)) {
                pos++;
            } else if (ch == '[') {
                tokens.add(new Token(TokenType.LBRACKET, "["));
                pos++;
            } else if (ch == ']') {
                tokens.add(new Token(TokenType.RBRACKET, "]"));
                pos++;
            } else if (ch == '{') {
                tokens.add(new Token(TokenType.LBRACE, "{"));
                pos++;
            } else if (ch == '}') {
                tokens.add(new Token(TokenType.RBRACE, "}"));
                pos++;
            } else if (ch == ':') {
                tokens.add(new Token(TokenType.COLON, ":"));
                pos++;
            } else if (ch == '=') {
                tokens.add(new Token(TokenType.EQUAL, "="));
                pos++;
            } else if (ch == ',') {
                tokens.add(new Token(TokenType.COMMA, ","));
                pos++;
            } else if (ch == '+') {
                tokens.add(new Token(TokenType.PLUS, "+"));
                pos++;
            } else if (ch == '-') {
                tokens.add(new Token(TokenType.MINUS, "-"));
                pos++;
            } else if (ch == '*') {
                tokens.add(new Token(TokenType.MULT, "*"));
                pos++;
            } else if (ch == '/') {
                tokens.add(new Token(TokenType.DIV, "/"));
                pos++;
            } else if (ch == '$') {
                int start = ++pos;
                while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
                    pos++;
                }
                if (pos < input.length() && input.charAt(pos) == '$') {
                    String name = input.substring(start, pos);
                    tokens.add(new Token(TokenType.DOLLAR_REF, name));
                    pos++;  // skip closing $
                } else {
                    throw new RuntimeException("Invalid reference at position " + pos);
                }
            } else if (Character.isDigit(ch)) {
                int start = pos;
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, pos)));
            } else if (ch == '"') {
                int start = ++pos;
                while (pos < input.length() && input.charAt(pos) != '"') pos++;
                tokens.add(new Token(TokenType.STRING, input.substring(start, pos)));
                pos++;  // skip closing "
            } else if (Character.isLetter(ch)) {
                int start = pos;
                while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_'))
                    pos++;
                tokens.add(new Token(TokenType.IDENTIFIER, input.substring(start, pos)));
            } else {
                throw new RuntimeException("Unknown character: " + ch);
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}

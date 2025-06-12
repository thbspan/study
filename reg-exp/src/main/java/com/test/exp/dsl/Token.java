package com.test.exp.dsl;

public class Token {
    public final TokenType type;

    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return String.format("Token(%s, '%s')", type, value);
    }
}

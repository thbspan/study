package com.test.exp.dsl;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() { return tokens.get(pos); }
    private Token next() { return tokens.get(pos++); }
    private boolean match(TokenType type) {
        if (peek().type == type) { pos++; return true; }
        return false;
    }
    private Token expect(TokenType type) {
        Token t = next();
        if (t.type != type) throw new RuntimeException("Expected " + type + ", got " + t.type);
        return t;
    }

    public List<FieldNode> parseFields() {
        List<FieldNode> fields = new ArrayList<>();
        while (peek().type != TokenType.EOF) {
            fields.add(parseField());
            match(TokenType.COMMA); // optional
        }
        return fields;
    }

    private FieldNode parseField() {
        expect(TokenType.LBRACKET);
        String name = expect(TokenType.IDENTIFIER).value;
        expect(TokenType.COLON);
        TypeNode type = parseType();
        expect(TokenType.EQUAL);
        ExpressionNode value = parseExpression();
        expect(TokenType.RBRACKET);
        return new FieldNode(name, type, value);
    }

    private TypeNode parseType() {
        String base = expect(TokenType.IDENTIFIER).value;
        if (base.equals("list")) {
            expect(TokenType.LBRACKET);
            TypeNode element = parseType();
            expect(TokenType.RBRACKET);
            return new ListTypeNode(element);
        } else if (base.equals("compose")) {
            return new ComposeTypeNode();
        } else {
            return new BasicTypeNode(base);
        }
    }

    private ExpressionNode parseExpression() {
        ExpressionNode left = parsePrimary();
        while (peek().type == TokenType.PLUS || peek().type == TokenType.MINUS ||
               peek().type == TokenType.MULT || peek().type == TokenType.DIV) {
            String op = next().value;
            ExpressionNode right = parsePrimary();
            left = new BinaryOpNode(op, left, right);
        }
        return left;
    }

    private ExpressionNode parsePrimary() {
        Token t = peek();
        switch (t.type) {
            case NUMBER:
                return new LiteralNode(Integer.parseInt(next().value));
            case STRING:
                return new LiteralNode(next().value);
            case DOLLAR_REF:
                return new ReferenceNode(next().value);
            case LBRACE:
                return parseComposeValue();
            default:
                throw new RuntimeException("Unexpected token in expression: " + t);
        }
    }

    private ExpressionNode parseComposeValue() {
        expect(TokenType.LBRACE);
        List<FieldNode> fields = new ArrayList<>();
        while (peek().type != TokenType.RBRACE) {
            fields.add(parseField());
            match(TokenType.COMMA);
        }
        expect(TokenType.RBRACE);
        return new ComposeValueNode(fields);
    }
}

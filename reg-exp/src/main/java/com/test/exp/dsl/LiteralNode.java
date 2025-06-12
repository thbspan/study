package com.test.exp.dsl;

class LiteralNode extends ExpressionNode {
    public final Object value;
    public LiteralNode(Object value) { this.value = value; }
}
package com.test.exp.dsl;

class ReferenceNode extends ExpressionNode {
    public final String refName;
    public ReferenceNode(String refName) { this.refName = refName; }
}
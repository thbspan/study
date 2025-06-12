package com.test.exp.dsl;

class BinaryOpNode extends ExpressionNode {
    public final String op;
    public final ExpressionNode left, right;
    public BinaryOpNode(String op, ExpressionNode left, ExpressionNode right) {
        this.op = op; this.left = left; this.right = right;
    }
}
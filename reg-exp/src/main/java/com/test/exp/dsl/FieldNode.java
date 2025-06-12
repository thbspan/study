package com.test.exp.dsl;

class FieldNode extends ASTNode {
    public final String name;
    public final TypeNode type;
    public final ExpressionNode value;

    public FieldNode(String name, TypeNode type, ExpressionNode value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
}
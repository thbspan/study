package com.test.exp.dsl;

class ListTypeNode extends TypeNode {
    public final TypeNode elementType;
    public ListTypeNode(TypeNode elementType) { this.elementType = elementType; }
}
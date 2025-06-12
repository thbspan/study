package com.test.exp.dsl;

class BasicTypeNode extends TypeNode {
    public final String name; // "int", "string"
    public BasicTypeNode(String name) { this.name = name; }
}
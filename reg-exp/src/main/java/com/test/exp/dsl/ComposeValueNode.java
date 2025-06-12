package com.test.exp.dsl;

import java.util.List;

class ComposeValueNode extends ExpressionNode {
    public final List<FieldNode> fields;
    public ComposeValueNode(List<FieldNode> fields) {
        this.fields = fields;
    }
}
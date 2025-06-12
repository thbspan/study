package com.test.exp.dsl;

import java.util.List;

public class DslDemo {
    public static void main(String[] args) {
        String code = "[a: int = 2],[b: string = \"ok\"],[c: compose = {x: int = 1, y: string = $b$}]";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<FieldNode> fields = parser.parseFields();
        for (FieldNode field : fields) {
            System.out.println(field);
        }
    }
}

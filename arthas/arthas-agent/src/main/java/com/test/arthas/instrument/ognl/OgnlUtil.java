package com.test.arthas.instrument.ognl;

import java.util.Map;

import ognl.DefaultClassResolver;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlException;

public class OgnlUtil {
    private final Map<?, ?> context;
    private final Object target;

    public OgnlUtil(Object target) {
        this.context = Ognl.createDefaultContext(target, new DefaultMemberAccess(true), new DefaultClassResolver(), new DefaultTypeConverter());
        this.target = target;
    }

    public Object getValue(String expression) throws IllegalArgumentException {
        try {
            return Ognl.getValue(expression, context, target);
        } catch (OgnlException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

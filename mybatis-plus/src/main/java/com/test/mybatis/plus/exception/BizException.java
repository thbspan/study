package com.test.mybatis.plus.exception;

/**
 * 业务异常基类
 */
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BizException(Throwable e) {
        super(e);
    }

    public BizException(String msg) {
        super(msg);
    }
}

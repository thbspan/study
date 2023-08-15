package com.test.mybatis.autoconstructor;

import org.apache.ibatis.annotations.Select;

public interface AutoConstructorMapper {

    @Select("SELECT * FROM subject WHERE id = #{id}")
    PrimitiveSubject getSubject(final int id);
}

package com.test.mybatis.plus.mapper;

import com.test.mybatis.plus.BaseMybatisPlusTestApplication;
import com.test.mybatis.plus.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class UserMapperTest extends BaseMybatisPlusTestApplication {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testSelect() {
        List<User> userList = userMapper.selectList(null);
        Assertions.assertEquals(5, userList.size());
        userList.forEach(System.out::println);
    }
}

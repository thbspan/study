package com.test.mybatis.cursor.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.cursor.Cursor;

import com.test.mybatis.cursor.entity.User;

@Mapper
public interface UserMapper {
    Cursor<User> selectAllByCursor();

    List<User> selectAll();

    int insertUser(User user);
}

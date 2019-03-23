package com.test.mybatis.cursor.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.mybatis.cursor.entity.User;
import com.test.mybatis.cursor.mapper.UserMapper;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public void testCursor() {
        int count = 0;
        for (User user : userMapper.selectAllByCursor()) {
            System.out.println(user);
            count++;
        }
        System.out.println("++++count" + count);
    }

    public void testInit() {
        for (int j = 0; j < 20; j++) {
            new Thread(() -> {
                for (int i = 0; i < 5_0000; i++) {
                    String base = UUID.randomUUID().toString();
                    User user = new User();
                    user.setName(base.substring(0, 5));
                    user.setCity(base.substring(9, 13));
                    user.setState(base.substring(14, 18));
                    userMapper.insertUser(user);
                }
            }).start();
        }
    }
}

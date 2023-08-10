package com.test.mybatis.flex.mapper;

import com.mybatisflex.core.query.QueryWrapper;
import com.test.mybatis.flex.BaseMybatisFlexTestApplication;
import com.test.mybatis.flex.entity.Account;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.test.mybatis.flex.entity.table.AccountTableDef.ACCOUNT;

class AccountMapperTest extends BaseMybatisFlexTestApplication {
    @Autowired
    private AccountMapper accountMapper;

    @Test
    void testSelect() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select()
                .where(ACCOUNT.AGE.eq(18));
        Account account = accountMapper.selectOneByQuery(queryWrapper);
        System.out.println(account);
        Assertions.assertNotNull(account);
    }
}

package com.test.mybatis.autoconstructor;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AutoConstructorTest {
    private static SqlSessionFactory sqlSessionFactory;

    @BeforeAll
    static void setUp() throws Exception {
        // create a SqlSessionFactory
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }

        // runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), "sql/create-db.sql");
    }
    @Test
    void fullyPopulatedSubject() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
            final Object subject = mapper.getSubject(1);
            assertNotNull(subject);
        }
    }

    public static void runScript(DataSource ds, String resource) throws IOException, SQLException {
        try (Connection connection = ds.getConnection()) {
            ScriptRunner runner = new ScriptRunner(connection);
            runner.setAutoCommit(true);
            runner.setStopOnError(false);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runScript(runner, resource);
        }
    }

    public static void runScript(ScriptRunner runner, String resource) throws IOException, SQLException {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            runner.runScript(reader);
        }
    }
}

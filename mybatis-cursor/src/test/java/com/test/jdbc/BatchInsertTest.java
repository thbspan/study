package com.test.jdbc;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

class BatchInsertTest {

    @Test
    void testMysql() throws SQLException {
        String url = "jdbc:mysql://localhost/test?rewriteBatchedStatements=true"; // 替换为你的 MySQL 数据库连接 URL
        String username = "root"; // 替换为你的数据库用户名
        String password = "my-secret-pw"; // 替换为你的数据库密码
        batchInsert(url, username, password);
    }

    @Test
    void testPostgresql() throws SQLException {
        String url = "jdbc:postgresql://localhost/postgres"; // 替换为你的 MySQL 数据库连接 URL
        String username = "root"; // 替换为你的数据库用户名
        String password = "123456"; // 替换为你的数据库密码
        batchInsert(url, username, password);
    }

    private void batchInsert(String url, String username, String password) throws SQLException {
        String insertSql = "INSERT INTO user_mysql (id, username, email, password, first_name, last_name, address, city, state, zip_code, country, phone_number, date_of_birth, gender, occupation, education_level, registration_date, last_login, is_active, is_admin, additional_field1, additional_field2) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                Random random = new Random();

                long start = System.currentTimeMillis();
                for (int i = 1; i <= 10000; i++) {
                    stmt.setInt(1, i);
                    stmt.setString(2, "username");
                    stmt.setString(3, "emailAddress");
                    stmt.setString(4, "password");
                    stmt.setString(5, "firstName");
                    stmt.setString(6, "lastName");
                    stmt.setString(7, "fullAddress");
                    stmt.setString(8, "city");
                    stmt.setString(9, "state");
                    stmt.setString(10, "zipCode");
                    stmt.setString(11, "china");
                    stmt.setString(12, "phoneNumber");
                    stmt.setDate(13, new Date(System.currentTimeMillis()));
                    stmt.setString(14, RandomString.make(6));
                    stmt.setString(15, "title");
                    stmt.setString(16, "course");
                    stmt.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setTimestamp(18, Timestamp.valueOf(LocalDateTime.now().minusDays(random.nextInt(365))));
                    stmt.setInt(19, 1);
                    stmt.setInt(20, 2);
                    stmt.setString(21, "sentence1");
                    stmt.setString(22, "sentence2");

                    stmt.addBatch();
                    if (i % 100 == 0) {
                        stmt.executeBatch(); // 执行批处理
                        stmt.clearBatch(); // 清空批处理
                    }
                }
                stmt.executeBatch();
                System.out.println("cost: " + (System.currentTimeMillis() - start) / 1000.0);
            }
        }
    }
}

package com.test.redis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

public class SentinelTest {

    @Test
    public void test() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(5);

        // 哨兵信息
        Set<String> sentinels = new HashSet<>(Arrays.asList("localhost:26378", "localhost:26379", "localhost:26380"));
        // 创建连接池
        try (JedisSentinelPool pool = new JedisSentinelPool("master-localhost", sentinels, poolConfig, null)) {
            // 获取客户端
            try (Jedis jedis = pool.getResource()) {
                // 执行两个命令
                jedis.set("mykey", "myvalue");
                String value = jedis.get("mykey");
                System.out.println(value);
            }
        }
    }
}

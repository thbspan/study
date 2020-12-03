package com.test.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.security.Keys;

public class JwtTest {

    private static final byte[] KEY_BYTES = "xcvjsdN SOFJodjfsodfi 47509885djfg -3)&*&(^%%&^98789".getBytes(StandardCharsets.UTF_8);

    private static final long TOKEN_EXPIRED_MILLI_SECOND = 5 * 60 * 1000;

    @Test
    public void test() {
        Key key = Keys.hmacShaKeyFor(KEY_BYTES);
        JwtBuilder builder = Jwts.builder().setId("999").setSubject("test")
                // 过期时间
                .setIssuedAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRED_MILLI_SECOND))
                .signWith(key, SignatureAlgorithm.HS256);
        System.out.println(builder.compact());
    }

    @Test
    public void testPutUserInfo() {
        Map<String, Object> claims = new HashMap<>();
        UserInfo userInfo =  new UserInfo();
        userInfo.setId(UUID.randomUUID().toString());
        userInfo.setName("测试账号");
        claims.put("user_info", userInfo);

        Key key = Keys.hmacShaKeyFor(KEY_BYTES);
        JwtBuilder builder = Jwts.builder().setClaims(claims)
                .setId("888")
                .setSubject("random")
                // 设置过期时间
                .setIssuedAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRED_MILLI_SECOND))
                .signWith(key, SignatureAlgorithm.HS256);
        String token = builder.compact();
        System.out.println(token);
        verify(token);
    }

    private void verify(String token) {
        Key key = Keys.hmacShaKeyFor(KEY_BYTES);
        Jws<Claims> jws = Jwts.parserBuilder()
                // 解析 JWT 的服务器与创建 JWT 的服务器的时钟不一定完全同步，此设置允许两台服务器最多有 1 分钟的时差
                .setAllowedClockSkewSeconds(60L)
                .setSigningKey(key)
                .deserializeJsonWith(new JacksonDeserializer<>(Collections.singletonMap("user_info", UserInfo.class)))
                .build().parseClaimsJws(token);

        Claims claims = jws.getBody();
        System.out.println(claims);
        System.out.println(claims.get("user_info", UserInfo.class));
    }
}

package com.example.demo.support;

import org.junit.jupiter.api.Assertions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

public class DbTestHelper {
    private final JdbcTemplate jdbcTemplate;

    public DbTestHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    public Long insertAndReturnLong(String sql, Object... args) {
        return queryForObject(Long.class, sql, args);
    }

    public <T> T queryForObject(Class<T> type, String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, type, args);
    }

    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        return jdbcTemplate.queryForList(sql, args);
    }

    public void assertRowExists(String sql, Object... args) {
        Assertions.assertFalse(queryForList(sql, args).isEmpty(), "Ожидалась хотя бы одна строка в БД");
    }

    public void assertValueEquals(Object expected, String sql, Object... args) {
        Object actual = jdbcTemplate.queryForObject(sql, Object.class, args);

        Assertions.assertEquals(expected, actual);
    }

    public <T> void assertObjectEquals(T expected, RowMapper<T> rowMapper, String sql, Object... args) {
        T actual = jdbcTemplate.queryForObject(sql, rowMapper, args);

        Assertions.assertEquals(expected, actual);
    }
}

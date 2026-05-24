package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends BaseIntegrationTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void logoutInvalidatesAccessAndRefreshTokensInRedisBlacklist() throws Exception {
        db.assertRowExists("select 1 from users where username = ?", "Ivan");
        db.assertValueEquals(2L, "select count(*) from users");

        /*
         * Здесь вставляем пользователя напрямую в PostgreSQL, чтобы тест не зависел от будущего
         * API регистрации. Так мы явно контролируем подготовку данных.
         */
        Long mariaId = db.insertAndReturnLong(
                "insert into users (password, username) values (?, ?) returning id",
                passwordEncoder.encode("secret"),
                "Maria"
        );
        db.insert("insert into user_roles (user_id, role) values (?, ?)", mariaId, "ROLE_USER");

        /*
         * Проверка объекта из БД удобна для сценариев, где важно не только HTTP-поведение,
         * но и фактическое состояние таблиц.
         */
        db.assertObjectEquals(
                new UserRoleRow("Maria", "ROLE_USER"),
                (rs, rowNum) -> new UserRoleRow(rs.getString("username"), rs.getString("role")),
                """
                        select u.username, r.role
                        from users u
                        join user_roles r on r.user_id = u.id
                        where u.username = ?
                        """,
                "Maria"
        );

        /*
         * Тело запроса читаем из src/test/resources, чтобы большие JSON-фикстуры не раздували тест.
         */
        String loginResponse = http.postJsonFileOk("/api/token", "json/auth/login-maria-request.json");

        String accessToken = json.extractString(loginResponse, "accessToken");
        String refreshToken = json.extractString(loginResponse, "refreshToken");

        http.getWithBearer("/api/secret", accessToken)
                .andExpect(status().isOk());

        /*
         * Logout кладет jti access/refresh токенов в Redis blacklist с TTL.
         * После этого подпись JWT остается корректной, но фильтр отклоняет токен по blacklist.
         */
        http.postJsonWithBearer("/api/token/logout", accessToken, """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken))
                .andExpect(status().isNoContent());

        String invalidAccessResponse = http.getWithBearer("/api/secret", accessToken)
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();
        json.assertFileStrictEquals("json/auth/invalid-token-response.json", invalidAccessResponse);

        http.postJson("/api/token/refresh", """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken))
                .andExpect(status().isUnauthorized());
    }

    private record UserRoleRow(String username, String role) {
    }
}

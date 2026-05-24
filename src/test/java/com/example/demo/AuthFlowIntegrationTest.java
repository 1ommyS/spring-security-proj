package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthFlowIntegrationTest extends BaseIntegrationTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void successAuth() throws Exception {
        db.assertRowExists("select 1 from users where username = ?", "Ivan");
        db.assertValueEquals(2L, "select count(*) from users");


        Long maria = db.insertAndReturnLong(
                "insert into users (password, username) values (?, ?) returning id",
                passwordEncoder.encode("secret"),
                "Maria"
        );
        db.insert("insert into user_roles(user_id, role) values (?, ?)", maria, "ROLE_USER");

        String loginResponse = http.postJsonFileOk("/api/token", "json/auth/login-maria-request.json");

        String accessToken = json.extractString(loginResponse, "accessToken");
        String refreshToken = json.extractString(loginResponse, "refreshToken");

        http.getWithBearer("/api/secret", accessToken)
                .andExpect(status().isOk());


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
}

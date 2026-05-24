package com.example.demo;

import com.example.demo.support.DbTestHelper;
import com.example.demo.support.HttpTestClient;
import com.example.demo.support.JsonTestHelper;
import com.example.demo.support.TestResourceReader;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("demo")
            .withUsername("demo")
            .withPassword("demo");


    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

//    protected TestResourceReader resources = new TestResourceReader();
//    protected DbTestHelper db =  new DbTestHelper(jdbcTemplate);
//    protected HttpTestClient http = new HttpTestClient(mockMvc, resources);
//    protected JsonTestHelper json = new JsonTestHelper(resources);

    protected DbTestHelper db;
    protected HttpTestClient http;
    protected JsonTestHelper json;
    protected TestResourceReader resources;

    @BeforeEach
    void setUpTestHelpers() {
        this.resources = new TestResourceReader();
        this.json = new JsonTestHelper(resources);
        this.db = new DbTestHelper(jdbcTemplate);
        this.http = new HttpTestClient(mockMvc, resources);
    }
    /*
    * минус тест контейнеров: образы стартуют на рандомных портах.
    * что делаем?
    * через DynamicPropertyRegistry переопределяем значения в application.yml для портов/параметров коннектов и тд
     */
    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

}

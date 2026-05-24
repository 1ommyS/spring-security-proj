package com.example.demo.support;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HttpTestClient {
    private final MockMvc mockMvc;
    private final TestResourceReader resources;

    public HttpTestClient(MockMvc mockMvc, TestResourceReader resources) {
        this.mockMvc = mockMvc;
        this.resources = resources;
    }

    public String postJsonOk(String path, String body) throws Exception {
        return postJson(path, body)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    public ResultActions postJson(String path, String body) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    public String postJsonFileOk(String path, String resourcePath) throws Exception {
        return postJsonOk(path, resources.read(resourcePath));
    }

    public ResultActions getWithBearer(String path, String token) throws Exception {
        return mockMvc.perform(get(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    public ResultActions postJsonWithBearer(String path, String token, String body) throws Exception {
        return mockMvc.perform(post(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}

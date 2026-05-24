package com.example.demo.support;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTestHelper {
    private final TestResourceReader resources;

    public JsonTestHelper(TestResourceReader resources) {
        this.resources = resources;
    }

    public void assertStrictEquals(String expectedJson, String actualJson) {
        try {
            JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
        } catch (JSONException e) {
            Assertions.fail("JSON не совпал", e);
        }
    }

    public void assertFileStrictEquals(String expectedResourcePath, String actualJson) {
        assertStrictEquals(resources.read(expectedResourcePath), actualJson);
    }

    public String extractString(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalStateException("В JSON нет поля: " + fieldName);
        }

        return matcher.group(1);
    }
}

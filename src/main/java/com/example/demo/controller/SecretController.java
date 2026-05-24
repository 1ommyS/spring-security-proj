package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/secret")
@RestController
public class SecretController {
    @GetMapping
    public String toAuthUsers() {
        return "Ты прошел проверку";
    }
}

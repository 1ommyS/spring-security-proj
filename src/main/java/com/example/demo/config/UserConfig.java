package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserConfig {

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var user = User.builder()
                .username("Ivan")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();

        var admin = User.builder()
                .username("Pasha")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN") // ROLE_ADMIN
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }
}

package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfNotExists(userRepository, passwordEncoder, "Ivan", "password", Set.of("ROLE_USER"));
            createUserIfNotExists(userRepository, passwordEncoder, "Misha", "admin", Set.of("ROLE_ADMIN"));
        };
    }

    private void createUserIfNotExists(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            Set<String> roles
    ) {
        if (userRepository.existsByUsername(username)) return;

        userRepository.save(new User(username, passwordEncoder.encode(password), roles));
    }
}

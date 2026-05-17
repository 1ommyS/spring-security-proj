package com.example.demo;

import com.example.demo.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/token")
@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    public LoginResponse login(@RequestBody LoginRequest login) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.username, login.password)
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String token = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResponse(token, refreshToken);
    }

    public record LoginResponse(
            String accessToken, String refreshToken
    ) {

    }

    public record LoginRequest(
            String username, String password
    ) {
    }
}

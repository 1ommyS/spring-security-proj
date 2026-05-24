package com.example.demo.controller;

import com.example.demo.controller.dto.LoginRequest;
import com.example.demo.controller.dto.LogoutRequest;
import com.example.demo.controller.dto.RefreshRequest;
import com.example.demo.controller.dto.TokenResponse;
import com.example.demo.service.AuthService;
import com.example.demo.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/token")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final AuthService authService;


    @PostMapping
    public TokenResponse login(@RequestBody LoginRequest login) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.username(), login.password())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String token = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new TokenResponse(token, refreshToken);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.refreshToken();

        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody LogoutRequest request
    ) {
        String accessToken = authorization.substring(7);

        if (accessToken != null) {
            jwtService.invalidateToken(accessToken);
        }

        jwtService.invalidateToken(request.refreshToken());

        return ResponseEntity.noContent().build();
    }
}

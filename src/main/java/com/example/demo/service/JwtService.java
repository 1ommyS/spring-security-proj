package com.example.demo.service;

import com.example.demo.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final TokenBlackListService tokenBlackListService;
    private final SecretKey secretKey;

    private final String TOKEN_TYPE_CLAIM = "type";

    private enum TokenTypes {
        ACCESS,
        REFRESH;
    }

    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expireAt = new Date(now.getTime() + jwtProperties.expirationAccess() * 1000);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claims(Map.of("roles", roles, TOKEN_TYPE_CLAIM, TokenTypes.ACCESS))
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + jwtProperties.expirationRefresh() * 1000);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .claims(Map.of(TOKEN_TYPE_CLAIM, TokenTypes.REFRESH))
                .expiration(expireAt)
                .signWith(secretKey)
                .compact();
    }

    public void invalidateToken(String token) {
        Claims claims = extractAllClaims(token);

        tokenBlackListService.blacklist(claims.getId(), claims.getExpiration());
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TokenTypes.ACCESS);
    }


    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TokenTypes.REFRESH);
    }


    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();

        return expiration.before(new Date());
    }

    private boolean isValid(String token, UserDetails userDetails, TokenTypes tokenType) {
        Claims claims = extractAllClaims(token);
        String userName = extractUsername(token);

        return userName.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && tokenType.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                && !tokenBlackListService.isBlackListed(claims.getId());
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Object roles = extractAllClaims(token).get("roles");

        if (!(roles instanceof List<?>)) return List.of();

        return ((List<?>) roles)
                .stream().map(String::valueOf).toList();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.example.notes_spring_security.config.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.refreshTokenExpirationMs}")
    private int refreshTokenExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove Bearer prefix
        }
        return null;
    }

    public String generateJwtTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String generateRefreshTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + refreshTokenExpirationMs))
                .signWith(key()) // Uses the same secret as access token
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }

    public Map<String, String> generateJwtTokenFromRefreshToken(String refreshToken) {
        if (validateRefreshToken(refreshToken)) {
            String username = getUserNameFromRefreshToken(refreshToken);
            Map<String, String> map = new HashMap<>();
            map.put("jwtToken", Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                    .signWith(key())
                    .compact());
            map.put("refreshToken", Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date((new Date()).getTime() + refreshTokenExpirationMs))
                    .signWith(key())
                    .compact());

            return map;
        }
        return null;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateJwtToken(refreshToken);
    }

    public String getUserNameFromRefreshToken(String token) {
        return getUserNameFromJwtToken(token);
    }
}
package com.example.SmartSeatBackend.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {


    private final SecretKey key;
    private final HelperMethods helper;
    // Spring injects "sec" right here, safely
    public JwtUtil(@Value("ZmFrZVNlY3JldEtleUZha2VTZWNyZXRLZXlGYWtlU2VjcmV0") String sec, HelperMethods helper) {
        this.key = Keys.hmacShaKeyFor(sec.getBytes(StandardCharsets.UTF_8));
        this.helper = helper;
    }

    public String generateToken(Long id ,String role) throws Exception {

        String encryptedId = helper.encrypt(id);
        return Jwts.builder()
                .claim("id",encryptedId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 10)))
                .signWith(key)
                .compact();
    }

    public String extractId(String token) {
        return String.valueOf(getClaims(token).get("id",String.class));
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // Tries to parse and verify signature
            return true; // If we get here, signature is valid
        } catch (Exception e) {
            // SignatureException, MalformedJwtException, ExpiredJwtException
            return false;
        }
    }
}
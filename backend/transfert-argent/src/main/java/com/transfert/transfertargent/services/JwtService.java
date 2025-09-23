package com.transfert.transfertargent.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import com.transfert.transfertargent.models.User;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "12345678901234567890123456789012"; // 32+ chars

    // Extraire le téléphone (ou clé unique) depuis le token
    public String extractTelephone(String token) {
        return extractClaims(token).getSubject();
    }

    // ✅ CORRECTION: Générer le JWT avec ID, prénom, nom, photo de profil ET ROLE
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getTelephone()) // clé unique
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10h
                .claim("id", user.getId())
                .claim("firstName", user.getPrenom())
                .claim("lastName", user.getNom())
                .claim("photoProfil", user.getPhotoProfil())
                .claim("role", user.getRole().name()) // ← AJOUT IMPORTANT
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ NOUVELLE MÉTHODE: Extraire le rôle depuis le token
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class);
    }

    // ✅ NOUVELLE MÉTHODE: Extraire l'ID utilisateur depuis le token
    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("id", Long.class);
    }

    // Vérifier si le token est valide
    public boolean isTokenValid(String token, User user) {
        final String username = extractTelephone(token);
        return (username.equals(user.getTelephone()) && !isTokenExpired(token));
    }

    // Vérifier si le token est expiré
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // Extraire les claims depuis le token
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Obtenir la clé de signature
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ NOUVELLE MÉTHODE: Vérifier si l'utilisateur est admin
    public boolean isAdmin(String token) {
        try {
            String role = extractRole(token);
            return "ADMIN".equals(role);
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ NOUVELLE MÉTHODE: Obtenir toutes les infos utilisateur depuis le token
    public UserInfo extractUserInfo(String token) {
        Claims claims = extractClaims(token);
        return new UserInfo(
                claims.get("id", Long.class),
                claims.get("firstName", String.class),
                claims.get("lastName", String.class),
                claims.get("role", String.class),
                claims.get("photoProfil", String.class),
                claims.getSubject() // telephone
        );
    }

    // ✅ CLASSE INTERNE pour structurer les infos utilisateur
    public static class UserInfo {
        public final Long id;
        public final String firstName;
        public final String lastName;
        public final String role;
        public final String photoProfil;
        public final String telephone;

        public UserInfo(Long id, String firstName, String lastName, String role, String photoProfil, String telephone) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.photoProfil = photoProfil;
            this.telephone = telephone;
        }
    }
}
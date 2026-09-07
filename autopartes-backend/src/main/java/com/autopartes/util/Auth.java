package com.autopartes.util;

import com.autopartes.model.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public class Auth {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private Auth() {}

    public static String crearToken(UUID id, Rol rol, long horasExpiracion) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(horasExpiracion, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(id.toString())
                .claim("rol", rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(KEY)
                .compact();
    }

    public static String crearToken(UUID id, Rol rol) {
        return crearToken(id, rol, 24);
    }

    public static Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static UUID extraerId(String token) {
        Claims claims = validarToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public static Rol extraerRol(String token) {
        Claims claims = validarToken(token);
        return Rol.valueOf(claims.get("rol", String.class));
    }

    public static boolean esTokenValido(String token) {
        try {
            validarToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

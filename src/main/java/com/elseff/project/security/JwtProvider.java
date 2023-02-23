package com.elseff.project.security;

import io.jsonwebtoken.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtProvider {

    @Value("${jwt.secret}")
    String jwtSecret;

    public String generateToken(String username) {
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Instant instant = now.plusDays(15).atZone(ZoneId.systemDefault()).toInstant();
        Date expiredDate = Date.from(instant);

        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");

        return Jwts.builder()
                .setHeader(header)
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.warn("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.warn("Unsupported jwt");
        } catch (MalformedJwtException malEx) {
            log.warn("Malformed jwt");
        } catch (SignatureException sEx) {
            log.warn("Invalid signature");
        } catch (Exception e) {
            log.warn("invalid token");
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (hasText(bearer) && bearer.startsWith("Bearer "))
            return bearer.substring(7);
        return null;
    }
}

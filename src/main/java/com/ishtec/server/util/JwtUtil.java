package com.ishtec.server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.ishtec.server.entities.UserToken;
import com.ishtec.server.repository.UserTokenRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class JwtUtil {

    @Value("${AUTH_SECRET:not_from_environment}")
    private String secretKey;

    private final UserTokenRepository userTokenRepository;

    public JwtUtil(UserTokenRepository userTokenRepository) {
        this.userTokenRepository = userTokenRepository;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenExists(String token, String username) {
        Optional<UserToken> userTokenOpt = userTokenRepository.findByToken(token);
        if (userTokenOpt.isPresent() && (userTokenOpt.get().getEmail().equalsIgnoreCase(username))) {
                return false;
            }
        return true;
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        String token = Jwts.builder().setClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
        userTokenRepository.deleteByEmail(subject);
        UserToken userToken = new UserToken(subject, token);
        userTokenRepository.save(userToken);
        return token;
    }

    public String getValidUsername(String token) {
        final String username = extractUsername(token);
        if (Boolean.FALSE.equals(isTokenExpired(token)) && Boolean.FALSE.equals(isTokenExists(token, username))) {
            return username;
        }
        return null;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getValidUsername(token);
        if (username != null) {
            return username.equals(userDetails.getUsername());
        }
        return false;
    }

    @PostConstruct
    public void checkSecretForTest() {
        if (this.secretKey.equals("not_from_environment")) {
            log.error("AUTH_SECRET not set in environment");
            System.exit(23);
        }
    }
}

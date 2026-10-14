package com.TrialCompass.TrialCompass.security;

import com.TrialCompass.TrialCompass.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

//creates and verifies tokens.
// has a: header, payload, signature
// payload is public, avoid sensitive data, read only
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final Duration lifetime;

    //@value reads application properties
    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration-minutes}") Long expirationMinutes) {
        //turns base 64 secret into a cryptographic key
        //must be 256 bits
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.lifetime = Duration.ofMinutes(expirationMinutes);
    }

    //builds a token for the user logging in
    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + lifetime.toMillis());

        return Jwts.builder()
                .subject(user.getEmail()) //says who the token is for
                .claim("uid", user.getId()) //the users Id
                .issuedAt(now) //when its created
                .expiration(expiry) //when it expires
                .signWith(secretKey) //signed with the generated key
                .compact(); //final string
    }

    //verify the signature and expiry
    //throw exception if token is malicious
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getLifetimeSeconds() {
        return lifetime.toSeconds();
    }


}

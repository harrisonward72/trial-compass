package com.TrialCompass.TrialCompass.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

//This runs one time per request | reads the token - tells spring if its valid its coming from the user
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //tokens arrive in the header first
        String header = request.getHeader("Authorization");

        if(header != null && header.startsWith("Bearer ")) {
            //drop bearer prefix to get the actual token
            String token = header.substring(7);

            try {
                Claims claims = jwtService.parseToken(token);
                //record the request is from email, args are who, credentials and roles - empty list for now no roles yet
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                //bad token, expired, forged - treat as anonymous security role will reject it later
                SecurityContextHolder.clearContext();
            }
        }
        //continues down the chain authenticated or not
        filterChain.doFilter(request, response);
    }
}

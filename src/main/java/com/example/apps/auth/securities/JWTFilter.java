package com.example.apps.auth.securities;

import java.io.IOException;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.settings.ApplicationProperties;
import com.example.settings.maindto.ApiErrorTemplate;
import com.example.settings.maindto.ApiTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class JWTFilter extends OncePerRequestFilter {

    private final String SECRET_KEY_STR;
    private final SecretKey SECRET_KEY;

    private final ObjectMapper objectMapper;

    @Autowired
    public JWTFilter(ApplicationProperties appProperties, ObjectMapper objectMapper) {
        this.SECRET_KEY_STR = appProperties.getSECRET_KEY();
        this.SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes());
        this.objectMapper = objectMapper.findAndRegisterModules();
    }

    @Autowired
    private JWTTokenBlacklistService jwtTokenBlacklistService;

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, MalformedJwtException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);

        if (jwtTokenBlacklistService.isAccessTokenBlacklisted(accessToken)) {
            throw new JwtException("Token is blacklisted");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            String tokenType = claims.get("type", String.class);

            if (!"access".equals(tokenType)) {
                throw new JwtException("Invalid token type for this endpoint");
            }
            String username = claims.getSubject();
            List<String> roles = (List<String>) claims.get("roles", List.class);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            var errorResponse = ApiTemplate.apiTemplateGenerator(
                    false,
                    401,
                    request.getRequestURI(),
                    ApiErrorTemplate.apiErrorTemplateGenerator(false, 401, request.getRequestURI(), e.getMessage()),
                    null);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (IllegalArgumentException e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            var errorResponse = ApiTemplate.apiTemplateGenerator(
                    false,
                    401,
                    request.getRequestURI(),
                    ApiErrorTemplate.apiErrorTemplateGenerator(false, 401, request.getRequestURI(), e.getMessage()),
                    null);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}

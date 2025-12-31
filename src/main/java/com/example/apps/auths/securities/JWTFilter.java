package com.example.apps.auths.securities;

import com.example.apps.auths.entities.Role;
import com.example.apps.auths.entities.User;
import com.example.tfs.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JWTFilter extends OncePerRequestFilter {

    private final SecretKey SECRET_KEY;

    @Autowired
    private JWTTokenBlacklistService jwtTokenBlacklistService;

    public JWTFilter(ApplicationProperties applicationProperties) {
        this.SECRET_KEY = applicationProperties.getJwtSigningKey();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, MalformedJwtException {

        String accessToken = null;
        String path = request.getServletPath();

        // Public endpoint'leri asaletle geçiyoruz
        if (path.startsWith("/rest/api/public/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token'ı Cookie içinden mühürleyelim
        if (request.getCookies() != null) {
            System.out.println("=== JWTFilter: Cookies received ===");
            for (var cookie : request.getCookies()) {
                System.out.println("Cookie: " + cookie.getName() + " = "
                        + (cookie.getValue().length() > 20 ? cookie.getValue().substring(0, 20) + "..."
                                : cookie.getValue()));
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                }
            }
        } else {
            System.out.println("=== JWTFilter: NO COOKIES RECEIVED for " + path + " ===");
        }

        // Token yoksa Spring Security'nin default auth mekanizması devreye girsin
        if (accessToken == null || accessToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Blacklist kontrolü
        if (jwtTokenBlacklistService.isAccessTokenBlacklisted(accessToken)) {
            // Token blacklist'te, devam et (Spring Security 401 dönecek)
            filterChain.doFilter(request, response);
            return;
        }

        try {
            System.out.println("JWTFilter: Parsing token...");
            Claims accessTokenBody = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            System.out.println("JWTFilter: Token parsed. Claims: " + accessTokenBody);

            String accessTokenType = accessTokenBody.get("type", String.class);
            if (!"access".equals(accessTokenType)) {
                System.out.println("JWTFilter: Wrong token type: " + accessTokenType);
                filterChain.doFilter(request, response);
                return;
            }

            String username = accessTokenBody.getSubject();
            List<String> roles = (List<String>) accessTokenBody.get("roles", List.class);

            // Safe extraction of ID
            Object idObj = accessTokenBody.get("id");
            System.out.println("JWTFilter: Raw ID claim type: " + (idObj == null ? "null" : idObj.getClass().getName())
                    + ", value: " + idObj);

            Long userId = null;
            if (idObj instanceof Integer) {
                userId = ((Integer) idObj).longValue();
            } else if (idObj instanceof Long) {
                userId = (Long) idObj;
            } else if (idObj != null) {
                try {
                    userId = Long.parseLong(idObj.toString());
                } catch (NumberFormatException e) {
                    System.out.println("JWTFilter: Could not parse ID: " + idObj);
                }
            }

            System.out.println("JWTFilter: Extracted userId: " + userId);

            if (userId == null) {
                System.out.println("JWTFilter: userId is null, skipping auth.");
                filterChain.doFilter(request, response);
                return;
            }

            // Efendim, CustomUserDetails için gerekli User nesnesini mühürlüyoruz
            User dummyUser = new User();
            dummyUser.setId(userId);
            dummyUser.setUsername(username);
            dummyUser.setEnabled(true);

            if (roles != null) {
                List<Role> roleEntities = roles.stream().map(roleName -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return r;
                }).collect(Collectors.toList());
                dummyUser.setRoles(roleEntities);
            }

            // Asil CustomUserDetails nesnesi artık hazır
            CustomUserDetails userDetails = new CustomUserDetails(dummyUser);

            // Principal kısmına userDetails vererek NullPointerException'ı bitiriyoruz
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("JWTFilter: Authentication successful for user: " + username + " (ID: " + userId + ")");
            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWTFilter: Token exception: " + e.getMessage());
            // Token geçersiz veya expired, Spring Security'nin auth mekanizması devreye
            // girsin
            // Bu sayede 401 yerine refresh token ile yeniden deneme şansı olacak
            filterChain.doFilter(request, response);
        }
    }

}
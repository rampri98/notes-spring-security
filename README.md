# Spring Security – JWT & Token-Based Security

## 1. Overview
- JWT (JSON Web Token) is a **compact, URL-safe token format** for transmitting claims between parties.
- Common in **stateless authentication** (no server session storage).
- Used in APIs, microservices, mobile apps.

---

## 2. Structure of JWT
- JWT consists of **three Base64URL-encoded parts**, separated by dots (`.`):
```
header.payload.signature
```

**Example:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTY5MDAwMDAwMCwiZXhwIjoxNjkwMDA2MDAwfQ.
QjhpZ2FmX3NlcmlvdXNseV9oYXNoZWRfdmFsdWU
```

**Parts:**
1. **Header** – Algorithm & token type.
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```
2. **Payload** – Claims (user data, roles, timestamps).
   ```json
   {
     "sub": "user1",
     "role": "ROLE_USER",
     "iat": 1690000000,
     "exp": 1690006000
   }
   ```
3. **Signature** – Verifies data integrity.
   ```
   HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secretKey)
   ```
---
## 3. Files
1. **JwtUtils**: Contains utility methods for generating, parsing, and validating JWTs. Include generating a token from a username, validating a JWT, and extracting the username from a token.
2. **AuthTokenFilter**: Filters incoming requests to check for a valid JWT in the header, setting the header, setting the authentication context if the token is valid. Extracts JWT from request header, validates it, and configures the Spring Security context with user details if the token is valid.
3. **AuthEntryPointJwt**: Provides custom handling for unauthorized requests, typically when authentication is required but not supplied or valid. When an unauthorized request is detected, it logs the error and returns a JSON response with an error message, status code, and the path attempted.
4. **SecurityConfig**: Sets up security filter chain, permitting or denying access based on paths and roles. It also configures session management to stateless, which is crucial for JWT usage.
---

## 4. Generating JWT in Spring Boot
Example using `io.jsonwebtoken` (JJWT library):
```java
public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("role", userDetails.getAuthorities())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
        .signWith(SignatureAlgorithm.HS256, secretKey)
        .compact();
}
```

---

## 5. Validating JWT
```java
public boolean validateToken(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}

public String extractUsername(String token) {
    return Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
}
```

---

## 6. Adding JWT Filter to Security Chain

**JWT Authentication Filter:**
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }
}
```

**Registering filter:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

---

## 7. Refresh Tokens
- **Problem**: Short-lived JWTs require frequent re-login.
- **Solution**: Use refresh tokens to obtain new access tokens without re-authentication.
- **Best practice**: Store refresh token securely (HTTP-only cookie or secure storage).

**Flow:**
1. User logs in → Receives **access token** (short-lived) + **refresh token** (long-lived).
2. Access token expires → Client sends refresh token to refresh endpoint.
3. Server validates refresh token → Issues new access token.

**Example:**
```java
@PostMapping("/refresh-token")
public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");
    if (validateRefreshToken(refreshToken)) {
        String username = extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = generateToken(userDetails);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}
```

---

## 8. Best Practices
✅ Keep JWT expiration short (e.g., 15–30 mins).  
✅ Store refresh tokens securely (HTTP-only cookies).  
✅ Always validate signature & expiration.  
✅ Never store sensitive data in JWT payload.  
✅ Use HTTPS to prevent token interception.


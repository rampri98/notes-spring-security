# Spring Security – Authorization Basics

## 1. Overview
- **Authorization** determines **what** an authenticated user is allowed to do.
- Works together with **Authentication** (who the user is).
- Implemented at multiple levels:
  1. **Role-based access control (RBAC)**
  2. **URL-based access control**
  3. **Method-level security**

---

## 2. Role-Based Access Control
- **Roles**: High-level grouping of permissions.
- Spring Security uses **`hasRole()`** and **`hasAuthority()`** methods.
- **Key Difference**:
  - `hasRole("ADMIN")` → internally adds `"ROLE_"` prefix (becomes `"ROLE_ADMIN"`).
  - `hasAuthority("ROLE_ADMIN")` → must use full authority name.

**Example** – Role-based access in `HttpSecurity`:
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
    .anyRequest().authenticated()
);
```

---

## 3. URL-Based Access Control
- Controlled through **`HttpSecurity`** configuration.
- Matches **request paths** to authorization rules.

**Example** – URL-based restrictions:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/profile/**").authenticated()
        )
        .formLogin(Customizer.withDefaults());
    return http.build();
}
```

---

## 4. Method-Level Security
- Allows **fine-grained control** on service-layer methods.
- Requires enabling with `@EnableMethodSecurity` (Spring Security 6+) or `@EnableGlobalMethodSecurity` (pre-6).

```java
@Configuration
@EnableMethodSecurity // For Spring Security 6+
public class SecurityConfig { }
```

**Annotations**:
- `@PreAuthorize` → Checks before method executes.
- `@PostAuthorize` → Checks after method executes (can inspect returned object).

**Examples**:
```java
@Service
public class UserService {

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        // Only admins can delete users
    }

    @PreAuthorize("#username == authentication.name")
    public User getUserProfile(String username) {
        // User can only see their own profile
    }

    @PostAuthorize("returnObject.owner == authentication.name")
    public Document getDocument(Long id) {
        // Only the owner can access document after retrieval
    }
}
```

---

## 5. Expression-Based Access Control
- Spring Security supports **SpEL (Spring Expression Language)** in annotations.
- Common variables:
  - `authentication` → current Authentication object.
  - `principal` → current UserDetails object.
  - `#paramName` → method parameter.
- Example:
```java
@PreAuthorize("#id == principal.id or hasRole('ADMIN')")
public void updateUser(Long id) { ... }
```

---

## 6. When to Use
✅ **Role-based** – For broad access rules (e.g., admin vs user).  
✅ **URL-based** – For securing endpoints at the request level.  
✅ **Method-level** – For securing business logic, independent of HTTP layer.

⚠️ Best practice: **Combine layers** – e.g., secure endpoints AND service methods.


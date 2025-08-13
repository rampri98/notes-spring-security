# Spring Security – Authentication Basics

## 1. Core components
  - `UserDetails` → Represents user information.
  - `UserDetailsService` → Loads user-specific data.
  - `PasswordEncoder` → Handles password hashing & verification.
  - `SecurityContextHolder` → Typically in Authentication storage.

## 2. `UserDetails`
- Interface representing **a user** in Spring Security.
- Must provide:
  - `getUsername()` → Unique user identifier.
  - `getPassword()` → Encrypted password.
  - `getAuthorities()` → Roles/permissions.
  - Account status checks (`isAccountNonExpired()`, etc.).
- Example:
```java
public class CustomUserDetails implements UserDetails {
    private final User user; // your entity

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
```

## 3. `UserDetailsService`
- Interface for **loading user data** given a username.
- Used by Spring Security during authentication.
- Must return a `UserDetails` object.
- Example:
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }
}
```
## 4. `UserDetailsManager`
- Interface that extends `UserDetailsService` and adds **user management operations**. 
- Allows creating, updating, deleting users, changing passwords, and checking if a user exists. 
- Often implemented by InMemoryUserDetailsManager or JdbcUserDetailsManager. 
- Useful when you need both authentication and dynamic user management.
- Example:

```java
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CustomUserDetailsManager implements UserDetailsManager {
  @Autowired
  private final InMemoryUserDetailsManager delegate;

  @Override
  public void createUser(UserDetails user) {
    delegate.createUser(user);
  }
  ...
}

```

## 5. `PasswordEncoder`
- Responsible for **hashing passwords** and **validating** entered passwords.
- **Why hash?**
  - Prevents storing plain-text passwords.
  - Protects users if DB is compromised.
- **Common implementations**:
  - `BCryptPasswordEncoder` → Strong & salted hashing (recommended).
  - `Pbkdf2PasswordEncoder`
  - `Argon2PasswordEncoder`
  - `NoOpPasswordEncoder` → For testing only (stores plain text).
- Example:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- **Password check flow**:
  1. User enters password.
  2. Encoder hashes the input.
  3. Hash is compared to stored hash.

## 6. In-Memory Authentication
- Useful for demos, prototypes, or small apps.
- Credentials are stored in memory (no DB).
- Example (modern `SecurityFilterChain`):
```java
@Bean
public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
    UserDetails user = User.withUsername("user")
        .password(encoder.encode("password"))
        .roles("USER")
        .build();

    UserDetails admin = User.withUsername("admin")
        .password(encoder.encode("admin123"))
        .roles("ADMIN")
        .build();

    return new InMemoryUserDetailsManager(user, admin);
}
```
- **Advantages**:
  - Quick setup.
  - No DB needed.
- **Disadvantages**:
  - Not persistent.
  - Not scalable.

## 7. How These Fit Together
1. User attempts login.
2. `AuthenticationManager` calls `UserDetailsService.loadUserByUsername()`.
3. Retrieved `UserDetails` contains username, hashed password, and authorities.
4. `PasswordEncoder` verifies provided password against stored hash.
5. If valid → authentication success, store in `SecurityContextHolder`.

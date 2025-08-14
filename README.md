# Spring Security – JPA-Based Authentication

## 1. Overview
- JPA-based authentication stores and retrieves user credentials from a **relational database** using **Spring Data JPA** instead of raw JDBC.
- Benefits over JDBC approach:
    - Works with entity classes and repositories.
    - No need to write SQL manually (unless customizing queries).
    - Integrates seamlessly with Spring Boot and ORM mapping.

## 2. Core Components
- **Entity classes**: Represent `User` and possibly `Role` tables.
- **Repository interface**: Extends `JpaRepository` to query users.
- **Custom `UserDetails` implementation**: Wraps the entity into Spring Security’s `UserDetails`.
- **Custom `UserDetailsService` implementation**: Reads user data from repository.
- - **Custom `UserDetailsManager` implementation**: Reads + writes user data from repository.
- **PasswordEncoder**: Encrypts passwords before saving, and verifies during authentication.

## 3. Example Entity Classes
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // getters and setters
}

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., ROLE_USER, ROLE_ADMIN

    // getters and setters
}
```

## 4. Repository Layer
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

## 5. `UserDetails` Implementation
```java
public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles()
                   .stream()
                   .map(role -> new SimpleGrantedAuthority(role.getName()))
                   .toList();
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getUsername(); }
}
```

## 6. `UserDetailsManager` Implementation (UserDetailsService + other implementations)
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


## 7. Security Configuration
- SecurityFilterChain
- PasswordEncoder
- UserDetailsManager
- AuthenticationManager

## 8. How It Works
1. User submits login credentials.
2. Spring Security calls `CustomUserDetailsService.loadUserByUsername()`.
3. Repository fetches `User` entity from DB.
4. `CustomUserDetails` wraps entity into Spring Security's format.
5. `PasswordEncoder` verifies the password.
6. On success → Authentication stored in `SecurityContextHolder`.

## 9. When to Use
✅ Best for:
- Applications already using **Spring Data JPA**.
- Domain-driven design where entities map directly to tables.
- Complex user-role relationships.

⚠️ Avoid if:
- Not using relational DB.
- Need extremely high-performance auth with minimal ORM overhead (JDBC might be faster).

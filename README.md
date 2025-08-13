# Spring Security – JDBC-Based Authentication

## 1. Overview
- JDBC-based authentication allows Spring Security to **retrieve user credentials and authorities from a relational database**.
- Works with **any database** that supports JDBC (MySQL, PostgreSQL, Oracle, etc.).
- Can use:
  1. **Default schema** expected by Spring Security.
  2. **Custom schema** with custom SQL queries.

## 2. Default Schema Approach
- Spring Security expects **two main tables**:
  1. **`users` table**
     ```sql
     CREATE TABLE users (
         username VARCHAR(50) NOT NULL PRIMARY KEY,
         password VARCHAR(100) NOT NULL,
         enabled BOOLEAN NOT NULL
     );
     ```
  2. **`authorities` table**
     ```sql
     CREATE TABLE authorities (
         username VARCHAR(50) NOT NULL,
         authority VARCHAR(50) NOT NULL,
         CONSTRAINT fk_authorities_users FOREIGN KEY(username) REFERENCES users(username)
     );
     ```
- **Default queries** used internally:
  ```sql
  SELECT username, password, enabled FROM users WHERE username = ?;
  SELECT username, authority FROM authorities WHERE username = ?;
  ```

## 3. Configuring JDBC Authentication
- **Modern `SecurityFilterChain` with `JdbcUserDetailsManager`**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
```
- Requires a **`DataSource` bean** (configured via `application.properties`):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## 4. Custom Queries (Custom Schema)
- If your table structure is different, you can set **custom queries**:
```java
@Bean
public UserDetailsService userDetailsService(DataSource dataSource) {
    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

    manager.setUsersByUsernameQuery(
        "SELECT user_name, user_pass, active FROM my_users WHERE user_name = ?"
    );

    manager.setAuthoritiesByUsernameQuery(
        "SELECT user_name, role FROM my_roles WHERE user_name = ?"
    );

    return manager;
}
```
- SQL must return columns in the **same order** Spring Security expects.

## 5. When to Use JDBC Authentication
✅ Good choice when:
- Credentials are stored in a **relational database**.
- You want to manage users via SQL tools or admin panels.
- No external identity provider (like OAuth2) is used.

⚠️ Not ideal when:
- Using NoSQL databases → Consider custom `UserDetailsService`.
- Using distributed token-based auth (e.g., JWT) → Prefer stateless authentication.

## 6. How the Flow Works
1. User submits username/password via form.
2. `JdbcUserDetailsManager` queries the database.
3. If username exists → retrieves encrypted password and authorities.
4. `PasswordEncoder` validates password.
5. On success → `Authentication` stored in `SecurityContextHolder`.

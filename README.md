# Spring Security – Configuration Approaches

## 1. Overview
- Spring Security can be configured in **two major ways**:
  1. **Legacy**: `WebSecurityConfigurerAdapter` (before Spring Security 5.7)
  2. **Modern**: `SecurityFilterChain` bean (recommended since Spring Security 5.7)
- Goal: Define how authentication, authorization, and other security aspects should work.

## 2. WebSecurityConfigurerAdapter (Legacy)
- Abstract class for configuring security by **overriding methods**.
- Common methods:
  - `configure(HttpSecurity http)` → Set rules for authentication/authorization.
  - `configure(AuthenticationManagerBuilder auth)` → Configure authentication providers.
- Example:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin();
    }
}
```
- **Status**: Deprecated in Spring Security 5.7 → use `SecurityFilterChain` instead.

## 3. SecurityFilterChain (Modern)
- Configured using a **`@Bean` method** in a `@Configuration` class.
- Gives more flexibility and works better with functional programming style.
- Must be used inside a class annotated with `@EnableWebSecurity` to activate Spring Security's web support.
- Example:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
```
- Advantages over legacy:
  - No need to extend a base class.
  - Easier integration with Spring Boot auto-configuration.
  - Promotes composition over inheritance.

## 4. Configuring `HttpSecurity`
`HttpSecurity` provides a fluent API to configure:
1. **Authorization rules**
   ```java
   http.authorizeHttpRequests(auth -> auth
       .requestMatchers("/admin/**").hasRole("ADMIN")
       .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
       .anyRequest().authenticated()
   );
   ```
2. **Session management**
   ```java
   http.sessionManagement(session -> session
       .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // useful for JWT APIs
   );
   ```
3. **Login methods**
  - **Form Login**:
    ```java
    http.formLogin(form -> form
        .loginPage("/login")
        .permitAll()
    );
    ```
  - **HTTP Basic**:
    ```java
    http.httpBasic(Customizer.withDefaults());
    ```
4. **Security Headers**
   ```java
   http.headers(headers -> headers
       .frameOptions().sameOrigin()
   );
   ```
5. **CSRF**
  - Enabled by default for web apps.
  - Disable for stateless APIs:
    ```java
    http.csrf(csrf -> csrf.disable());
    ```

## 5. CSRF Protection
- **Purpose**: Prevent **Cross-Site Request Forgery** — where an attacker tricks a logged-in user into making unwanted requests.
- **How it works**:
  - Server issues a unique token for each session/request.
  - Token must be sent with every modifying request (POST, PUT, DELETE).
  - Spring Security automatically checks this token.
- **Enabled by default** in Spring Security.
- **When to disable**:
  - Stateless REST APIs (using JWT, OAuth2, etc.).
  - Services where all requests are authenticated via tokens/headers, not cookies.
- Example of disabling:
```java
http.csrf(csrf -> csrf.disable());
```
⚠️ Only disable CSRF if you fully understand the security implications.

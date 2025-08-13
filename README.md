# Spring Security – Core Concepts

## 1. Overview & Purpose
- **Spring Security** is a **framework** that provides **authentication, authorization, and protection** against common attacks (CSRF, session fixation, etc.).
- Designed to integrate seamlessly with **Spring-based applications**, but can also be used in non-Spring projects.
- **Key advantage**: Highly customizable via filter-based architecture.

## 2. High-Level Architecture
- **Core idea**: Requests pass through a **chain of security filters** before reaching application logic.
- Three main layers:
    1. **Servlet Filter Chain** → Delegates to Spring Security filter chain.
    2. **Authentication layer** → Identifies who the user is.
    3. **Authorization layer** → Determines what the user can do.
- **Primary components**:
    - **Security Filters** (entry point for security logic)
    - **AuthenticationManager** & **AuthenticationProvider**
    - **SecurityContext & SecurityContextHolder**
    - **AccessDecisionManager**

## 3. Flow of Spring Security
1. **Incoming request** hits **`DelegatingFilterProxy`** (configured in `web.xml` or auto-registered in Spring Boot).
2. **DelegatingFilterProxy** delegates to `FilterChainProxy` (Spring Security's own filter chain).
3. Filters process request in order:
    - **Authentication Filters** (e.g., `UsernamePasswordAuthenticationFilter`)
    - **Authorization Filters** (e.g., `FilterSecurityInterceptor`)
4. If authentication succeeds:
    - User details stored in **SecurityContextHolder** (thread-local).
5. If authorization succeeds:
    - Request reaches the controller.
6. On logout or session end:
    - **SecurityContext** is cleared.

## 4. Authentication vs Authorization
- **Authentication**:
    - **Definition**: Verifying the *identity* of a user (username/password, tokens, etc.).
    - **Example**: "Is this really Alice?"
    - **Implementation**: `AuthenticationManager` & `AuthenticationProvider` perform checks.
- **Authorization**:
    - **Definition**: Deciding what actions an *authenticated* user is allowed to perform.
    - **Example**: "Can Alice delete this file?"
    - **Implementation**: `AccessDecisionManager` and configuration annotations (`@PreAuthorize`, `@Secured`).

## 5. Security Filters & Filter Chain
- **Filter-based architecture** means each security concern is handled by a dedicated filter.
- Common filters (executed in a specific order):
    - `SecurityContextPersistenceFilter` – Restores SecurityContext for request.
    - `UsernamePasswordAuthenticationFilter` – Handles login form submissions.
    - `BasicAuthenticationFilter` – Handles HTTP Basic auth.
    - `BearerTokenAuthenticationFilter` – Handles JWT or OAuth2 tokens.
    - `ExceptionTranslationFilter` – Catches security exceptions and redirects/returns error.
    - `FilterSecurityInterceptor` – Final authorization check.
- **Order matters** — misordering can break authentication/authorization flow.

**Code Example – Custom Filter Registration**
```java
http.addFilterBefore(new CustomFilter(), UsernamePasswordAuthenticationFilter.class);
```

## 6. DelegatingFilterProxy
- A **bridge** between the servlet container's filter chain and Spring's application context.
- Registers a filter in `web.xml` (or auto in Boot) that **delegates** to a bean (`springSecurityFilterChain`).


## 7. Other Key Terms
- **SecurityContext**: Holds authentication info for the current request.
- **SecurityContextHolder**: Provides static access to the current SecurityContext.
- **Authentication** object:
    - Contains `principal` (user details), `credentials`, and `authorities`.
- **GrantedAuthority**: Represents a role/privilege (`ROLE_ADMIN`, `ROLE_USER`).
- **AuthenticationManager**:
    - Entry point for authentication logic.
    - Delegates to one or more **AuthenticationProviders**.
- **AuthenticationProvider**:
    - Performs actual authentication.
    - Returns a fully authenticated `Authentication` object if successful.

## 8. Default Behavior
- Spring Boot auto-enables **form-based login** when `spring-boot-starter-security` is on the classpath.
- A **built-in login page** is available at `/login`.
- **All endpoints are secured** by default — authentication is required unless explicitly permitted.
- A default user is created with a **random password**, printed in the console.
- You can set up default username and password in the application.properties.

```properties
spring.security.user.name=admin
spring.security.user.password=admin123
```
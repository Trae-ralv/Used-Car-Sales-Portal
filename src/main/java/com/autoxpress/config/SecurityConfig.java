package com.autoxpress.config;

import com.autoxpress.model.UserModel;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Define public endpoints
        RequestMatcher publicEndpoints = new OrRequestMatcher(
            new AntPathRequestMatcher("/"),
            new AntPathRequestMatcher("/cars"),
            new AntPathRequestMatcher("/cars/**"),
            new AntPathRequestMatcher("/cars/search"),
            new AntPathRequestMatcher("/about"),
            new AntPathRequestMatcher("/contact"),
            new AntPathRequestMatcher("/register"),
            new AntPathRequestMatcher("/login"),
            new AntPathRequestMatcher("/password-reset-request"),
            new AntPathRequestMatcher("/password-reset"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**")
        );

        // Define matcher for non-public endpoints
        RequestMatcher nonPublicEndpoints = new NegatedRequestMatcher(publicEndpoints);

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/cars", "/cars/**", "/cars/search", "/about", "/contact", "/register", "/login", 
                                 "/password-reset-request", "/password-reset", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/cars/*/appointment", "/cars/*/report", "/user/**", "/cars/post").hasRole("USER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout")
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .expiredUrl("/login?expired")
            )
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        } else {
                            response.sendRedirect("/login");
                        }
                    },
                    nonPublicEndpoints
                )
                .accessDeniedHandler(accessDeniedHandler())
            );
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            // Get the current authentication object
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if the user is authenticated and has the USER role
            if (authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"))) {
                // Redirect USER role users to /user/dashboard with an error message
                response.sendRedirect("/user/dashboard?error=Access%20denied.%20You%20do%20not%20have%20permission%20to%20access%20this%20page.");
            } else {
                // For unauthenticated users or other roles, redirect to login page
                response.sendRedirect("/login?error=Please%20log%20in%20to%20access%20this%20page.");
            }
        };
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String userId = authentication.getName(); // Now userId instead of username
            UserModel user = userRepository.findByUserId(Long.parseLong(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
            String redirectUrl = user.getUserRole() == UserModel.Role.USER ? "/user/dashboard" : "/admin/dashboard";
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // Load user by username (as entered in login form)
            UserModel user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            // Use userId as the principal in UserDetails
            return User.withUsername(String.valueOf(user.getUserId()))
                      .password(user.getUserPassword())
                      .roles(user.getUserRole().name())
                      .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
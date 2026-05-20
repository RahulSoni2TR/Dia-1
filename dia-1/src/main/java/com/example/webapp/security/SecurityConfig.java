package com.example.webapp.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import com.example.webapp.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Secure password encoding
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/static/**", "/files/**","/uploads/**").permitAll()
                // allow public loading of product details (GET)
                .requestMatchers(HttpMethod.GET,"/get-estimate", "/loadProduct/**", "/load-product/**", "/loadProductByDesignNo/**","/available-order-ids"," \"/getTagsPdf/**\"").permitAll()
                // allow anonymous updates (PUT) — BE CAREFUL: this opens modification without auth
                .requestMatchers(HttpMethod.PUT, "/updateProduct/**", "/update-product/**","/verify/**", "/batch-update").hasRole("ADMIN")
                .requestMatchers(
                        HttpMethod.POST,
                        "/tags/custom/pdf"
                    ).permitAll()
                .requestMatchers(HttpMethod.GET,  "/getEstimate/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/logEnquiry").permitAll()
                .requestMatchers(HttpMethod.POST, "/logSale").permitAll()
                // keep login/registration public
                .requestMatchers("/login", "/register", "/logout", "/process-login", "*.css","/get-security-question","/reset-password","/verify-security-answer",
                		"/reset-ns-password","/modify-product",
                	"proxy-image","/loadProductByDesignNo/**","/load-product/**").permitAll() 
                .requestMatchers("/add-product", "/remove-product", "/set-price","/available-order-ids","/categories",
                		"/getCategoryNameById","/api/enquiries","/enquiries","/sales/logs","/verify","/frequency", "/batch-update").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/enquiries/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/sales-logs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/rate-history/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/rates").permitAll()
                .requestMatchers("/public-rates").permitAll()
                .requestMatchers("/view-product").authenticated()
                .requestMatchers("/price-history").authenticated()
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login-process")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login")
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            .and()
            .csrf()
                .disable() // Disable CSRF for APIs or WebSocket compatibility
            .authenticationProvider(authenticationProvider())
        .headers()
        .frameOptions()
        .sameOrigin();
        return http.build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("*")); // Frontend origin
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Remove this line to avoid IllegalArgumentException:
        // configuration.setAllowedOrigins(Arrays.asList("*"));

        // Allow all origins with patterns while allowing credentials
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
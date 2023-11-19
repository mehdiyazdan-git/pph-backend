package com.armaninvestment.parsparandreporter.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableGlobalMethodSecurity(
        // securedEnabled = true,
        // jsr250Enabled = true,
        prePostEnabled = true)
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService,
                             AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests().requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated();
        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/api/**")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/products/**")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/products/?page={page}&size={size}")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/customers/{customerId}/{year}/monthly-sales")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/customers/{customerId}/{year}/monthly-payments")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/customers/{year}/monthly-payments")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/customers/{year}/monthly-sales")
//                        .allowedOrigins("http://5.238.251.36:3000")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/monthly-report/{year}/{month}")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/drop-down/customers")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//                registry.addMapping("/api/drop-down/contracts/{customerId}")
//                        .allowedOrigins("http://5.238.156.129:3000")
//                        .allowedMethods("GET")
//                        .allowedHeaders("Content-Type", "Authorization")
//                        .allowCredentials(true)
//                        .maxAge(3600);
//            }
//
//        };
//    }


}

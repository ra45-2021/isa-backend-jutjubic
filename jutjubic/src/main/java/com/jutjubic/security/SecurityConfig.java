package com.jutjubic.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // DODAJ OVO: Dozvoljava browseru da prikazuje sadržaj (bitno za h2-console i slike)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                        // Proveri da li ovde fali kosa crta na početku ponekad, ali ovo je u redu:
                        .requestMatchers("/uploads/**", "/media/**").permitAll()
                        .anyRequest().permitAll() // PRIVREMENO: Dok ne sredimo registraciju, dozvoli sve da vidimo da li slike rade
                );

        return http.build();
    }
}

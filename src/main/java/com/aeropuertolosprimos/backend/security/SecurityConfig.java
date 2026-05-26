package com.aeropuertolosprimos.backend.security;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // PUBLICOS
                        //.requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/catalogos/**").permitAll()

                        // CONSULTAS PUBLICAS
                        //.requestMatchers("/vuelos/consulta/**").permitAll()

                        // ADMIN SISTEMA
                        //.requestMatchers("/empleados/**")
                        //.hasRole("ADMIN_SISTEMA")
                        //.requestMatchers("/empleados/**").permitAll()

                        // ADMIN AEROLINEA
                        //.requestMatchers("/tripulaciones/**")
                        //.hasRole("ADMIN_AEROLINEA")

                        //.requestMatchers("/disponibilidades/**")
                        //.hasRole("ADMIN_AEROLINEA")

                        //.requestMatchers("/aerolineas/**")
                        //.hasRole("ADMIN_AEROLINEA")
                        //.requestMatchers("/aerolineas/**").permitAll()

                        //.requestMatchers("/destinos-autorizados/**")
                       // .hasRole("ADMIN_AEROLINEA")

                        //.requestMatchers("/avion/**")
                        //.hasRole("ADMIN_AEROLINEA")

                        //.requestMatchers("/modelo-avion/**")
                        //.hasRole("ADMIN_AEROLINEA")

                        //.requestMatchers("/config-clase-filas-avion/**")
                        //.hasRole("ADMIN_AEROLINEA")

                        //.requestMatchers("/asiento-ubi/**")
                        //.hasRole("ADMIN_AEROLINEA")

                        // ADMIN ABORDAJE
                        //.requestMatchers("/abordaje/**")
                        //.hasRole("ADMIN_ABORDAJE")

                        // CONSULTAS AEROLINEA
                        //.requestMatchers("/reportes/**")
                        //.hasRole("CONSULTAS_AEROLINEA")

                        // AEROPUERTOS
                        //.requestMatchers("/aeropuertos/**")
                        //.authenticated()

                        .requestMatchers("/empleados/**").permitAll()
                        .requestMatchers("/aerolineas/**").permitAll()
                        .requestMatchers("/tripulaciones/**").permitAll()
                        .requestMatchers("/disponibilidades/**").permitAll()
                        .requestMatchers("/destinos-autorizados/**").permitAll()
                        .requestMatchers("/avion/**").permitAll()
                        .requestMatchers("/modelo-avion/**").permitAll()
                        .requestMatchers("/config-clase-filas-avion/**").permitAll()
                        .requestMatchers("/asiento-ubi/**").permitAll()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/pasajeros/**").permitAll()
                        .requestMatchers("/aeropuertos/**").permitAll()
                        .requestMatchers("/modelo-avion/**").permitAll()
                        .requestMatchers("/catalogos/**").permitAll()
                        .requestMatchers("/vuelos/**").permitAll()
                        .requestMatchers("/vuelos-operados/**").permitAll()
                        .requestMatchers("/asientos-vuelo/**").permitAll()
                        .requestMatchers("/reservas/**").permitAll()
                        .requestMatchers("/abordaje/**").permitAll()

                        .requestMatchers("/pagos/**").permitAll()

                        .requestMatchers("/checkin/**").permitAll()

                        .requestMatchers("/reportes/**").permitAll()
                        .requestMatchers("/consultas/**").permitAll()


                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://main.dpungtf1hfksr.amplifyapp.com",
                "https://aeropuertolosprimos.me",
                "https://www.aeropuertolosprimos.me"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(List.of("Authorization"));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
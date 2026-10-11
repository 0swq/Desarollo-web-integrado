package com.autopartes.config;

import com.autopartes.filter.JwtAuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, denied) ->
                                response.setStatus(HttpStatus.FORBIDDEN.value())))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/stocks/bajo-stock")
                            .hasAnyRole("ADMIN", "EMPLEADO")

                        .requestMatchers(HttpMethod.GET,
                                "/api/productos/**",
                                "/api/categorias/**",
                                "/api/proveedores/**",
                                "/api/marcas-vehiculos/**",
                                "/api/modelos-vehiculos/**",
                                "/api/compatibilidades-vehiculos/**",
                                "/api/producto-categorias/producto/**",
                                "/api/producto-categorias/categoria/**",
                                "/api/stocks",
                                "/api/stocks/*").permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/productos/**", "/api/categorias/**", "/api/proveedores/**",
                                "/api/marcas-vehiculos/**", "/api/modelos-vehiculos/**",
                                "/api/compatibilidades-vehiculos/**",
                                "/api/producto-categorias/**",
                                "/api/stocks/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/productos/**", "/api/categorias/**", "/api/proveedores/**",
                                "/api/marcas-vehiculos/**", "/api/modelos-vehiculos/**",
                                "/api/compatibilidades-vehiculos/**",
                                "/api/stocks/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/productos/**", "/api/categorias/**", "/api/proveedores/**",
                                "/api/marcas-vehiculos/**", "/api/modelos-vehiculos/**",
                                "/api/compatibilidades-vehiculos/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/productos/**", "/api/categorias/**", "/api/proveedores/**",
                                "/api/marcas-vehiculos/**", "/api/modelos-vehiculos/**",
                                "/api/compatibilidades-vehiculos/**",
                                "/api/producto-categorias/**").hasAnyRole("ADMIN", "EMPLEADO")

                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/password").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/pagos/**").hasAnyRole("ADMIN", "EMPLEADO")

                        .requestMatchers(HttpMethod.GET,
                                "/api/ordenes", "/api/ordenes/*",
                                "/api/ordenes/numero/*", "/api/ordenes/estado/*")
                            .hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/ordenes/*/estado")
                            .hasAnyRole("ADMIN", "EMPLEADO")

                        .requestMatchers("/api/carritos/**").authenticated()
                        .requestMatchers("/api/ordenes/usuario/**").authenticated()

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
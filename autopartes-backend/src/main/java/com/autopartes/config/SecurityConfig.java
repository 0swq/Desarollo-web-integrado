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

    /** Sin token o con token invalido -> 401. Rol insuficiente -> 403. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        // setStatus y no sendError: sendError dispara un dispatch a /error que
                        // vuelve a pasar por la cadena de seguridad sin contexto y terminaba
                        // devolviendo 401 en lugar de 403.
                        .accessDeniedHandler((request, response, denied) ->
                                response.setStatus(HttpStatus.FORBIDDEN.value())))
                .authorizeHttpRequests(auth -> auth

                        // ---------- 1. Documentacion ----------
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ---------- 2. Autenticacion (publico) ----------
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()

                        // ---------- 3. Backoffice: debe ir ANTES de la lectura publica de stock,
                        //             porque /api/stocks/* la incluiria de no ser asi ----------
                        .requestMatchers(HttpMethod.GET, "/api/stocks/bajo-stock")
                            .hasAnyRole("ADMIN", "EMPLEADO")

                        // ---------- 4. Lectura de catalogo (publico, sin token) ----------
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

                        // ---------- 5. Escritura de catalogo (ADMIN o EMPLEADO) ----------
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

                        // ---------- 6. Usuarios ----------
                        // El titular puede cambiar su propia contrasena; el resto es solo ADMIN.
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/password").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // ---------- 7. Pagos (ADMIN o EMPLEADO) ----------
                        .requestMatchers("/api/pagos/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // ---------- 8. Ordenes de backoffice (ADMIN o EMPLEADO).
                        //             Los patrones de un solo segmento NO capturan
                        //             /api/ordenes/usuario/{usuarioId}, que es del titular. ----------
                        .requestMatchers(HttpMethod.GET,
                                "/api/ordenes", "/api/ordenes/*",
                                "/api/ordenes/numero/*", "/api/ordenes/estado/*")
                            .hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PATCH, "/api/ordenes/*/estado")
                            .hasAnyRole("ADMIN", "EMPLEADO")

                        // ---------- 9. Carrito y ordenes propias (cualquier autenticado;
                        //             el controller valida que le pertenezcan) ----------
                        .requestMatchers("/api/carritos/**").authenticated()
                        .requestMatchers("/api/ordenes/usuario/**").authenticated()

                        // ---------- 10. Cualquier otra ruta requiere autenticacion ----------
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
package com.example.ProyectoPeluqueria.Config;

import com.example.ProyectoPeluqueria.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.DispatcherType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final UserDetailsServiceImpl userDetailsService;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(authz ->
//                        authz
//                                .requestMatchers("/**").permitAll()  // Permite todos los endpoints sin autenticación
//                                .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(false);
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    //Autentica los Usuarios al inciar sesion
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
//
//        provider.setPasswordEncoder(passwordEncoder());
//
//        return provider;
//    }
//
//    //Autentica los Usuarios con la bdd
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
//            throws Exception {
//        return config.getAuthenticationManager();
//    }
private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Agregamos TU configuración de CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. Desactivamos CSRF (obligatorio en APIs REST)
                .csrf(AbstractHttpConfigurer::disable)
                // 3. Configuramos las rutas
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos del frontend
                        .requestMatchers("/", "/index.html", "/error").permitAll()
                        .requestMatchers("/*.js", "/*.mjs", "/*.css", "/*.html", "/*.json", "/*.map").permitAll()
                        .requestMatchers("/*.ico", "/*.png", "/*.jpg", "/*.jpeg", "/*.svg", "/*.gif").permitAll()
                        .requestMatchers("/*.woff", "/*.woff2", "/*.ttf", "/*.eot").permitAll()
                        .requestMatchers("/assets/**", "/media/**", "/**/*.js", "/**/*.css", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.svg", "/**/*.ico", "/**/*.woff", "/**/*.woff2").permitAll()
                        // Rutas públicas API
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/webhooks/whatsapp/**").permitAll()
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Catálogo público
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**", "/api/comercios/**",
                                "/api/horarios/**", "/api/resenias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/turnos/disponibilidad").permitAll()
                        // Rutas protegidas API
                        .requestMatchers(HttpMethod.POST, "/api/turnos/*/recordatorio-prueba")
                                .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/turnos").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/turnos/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/turnos/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/turnos/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers("/api/cancelaciones/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/resenias")
                                .hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/resenias/**")
                                .hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/horarios/bloqueos")
                                .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/horarios/bloqueos/**")
                                .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/servicios/**", "/api/horarios/**",
                                "/api/comercios/**").hasAnyAuthority("COMERCIO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**", "/api/horarios/**",
                                "/api/comercios/**").hasAnyAuthority("COMERCIO", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**", "/api/horarios/**",
                                "/api/comercios/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAnyAuthority("CLIENTE", "ADMIN")
                        .requestMatchers("/api/usuarios/**").hasAuthority("ADMIN")
                        // Cualquier otra ruta NO /api -> pasar al frontend SPA
                        .requestMatchers("/**").permitAll()
                )
                // 4. Política Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                // 5. Acá ponemos a tu "Patovica" (El filtro JWT)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Autenticación requerida\",\"path\":\""
                    + request.getRequestURI() + "\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"No tienes permisos para realizar esta operación\",\"path\":\""
                    + request.getRequestURI() + "\"}");
        };
    }

    // método de CORS que estaba perfecto
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

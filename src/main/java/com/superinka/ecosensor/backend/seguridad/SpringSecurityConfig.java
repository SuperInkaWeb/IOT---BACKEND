package com.superinka.ecosensor.backend.seguridad;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.core.*;
@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

	
	private final UsuarioRepository usuarioRepository;
    
	@Bean
	public JwtAuthConverter jwtAuthConverter(UsuarioRepository usuarioRepository) {
	    return new JwtAuthConverter(usuarioRepository);
	}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthConverter jwtAuthConverter) throws Exception {
    	
    	JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    	converter.setJwtGrantedAuthoritiesConverter(jwtAuthConverter);
    	
    	
    	
        http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        	
        .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(auth -> auth
            		
            		//.requestMatchers("/api/admin/**").hasRole("ADMIN")
            		.requestMatchers("/api/admin/**").permitAll()
            		
            		 .requestMatchers("/ws/**").permitAll()
                     .requestMatchers("/ws").permitAll()

                     .requestMatchers("/api/pagos/webhook").permitAll()
                     .requestMatchers("/api/pagos/iniciar").authenticated()
            		
                     .requestMatchers("/api/dashboard/**").permitAll()
            		
                    // endpoints públicos
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers("/api/empresas/registro").permitAll()
                    
                    .requestMatchers("/api/usuarios/completar-perfil").permitAll()
                    

                    // sensores pueden enviar datos
                    .requestMatchers(HttpMethod.POST, "/api/lecturas").permitAll()
                    .requestMatchers("/api/lecturas/**").permitAll()
                    
                    // solo empresas
                    .requestMatchers("/api/sensores/**").hasAnyRole("ADMIN", "OPERADOR", "VISOR")
                   
                    

                    // todo lo demás requiere login
                    .anyRequest().authenticated()
                
            )
            .oauth2ResourceServer(oauth -> oauth
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
            )
        
        .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            );

        return http.build();
    }

    
    @Bean
    public JwtDecoder jwtDecoder() {

        String issuer = "https://dev-6u1q0s2nx3pub4do.us.auth0.com/";

        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtAudienceValidator("https://ecosensor-api");

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOriginPatterns(List.of("http://localhost:4200",
        		"https://ecosensor-inka.netlify.app", 
                "https://*.netlify.app"));
        	
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // si usas cookies o auth
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    
   
}
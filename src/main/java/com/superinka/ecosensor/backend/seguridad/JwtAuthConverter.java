package com.superinka.ecosensor.backend.seguridad;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
	
	private final UsuarioRepository usuarioRepository;
	
	private final Map<String, String> roleCache = new ConcurrentHashMap<>();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

    	
    	System.out.println(">>> DEBUG TOKEN CLAIMS: " + jwt.getClaims());

        // 2. Intentar sacar el email de varias formas comunes en Auth0
    	String email = jwt.getClaim("email");
    	
    	
    	if (email == null) {
            email = jwt.getClaimAsString("https://ecosensor-api/email");
        }
    	
    	// 3. Si sigue siendo nulo, intentar con el subject (pero esto es lo que queremos evitar)
        if (email == null) {
            email = jwt.getSubject();
        }

        
        if (email == null) {
            System.out.println(">>> ALERTA: Auth0 no mandó el email en ninguna claim. Usando SUB como fallback.");
            email = jwt.getSubject();
        }
        
        if (email == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_VISOR"));
        }

        final String finalEmail = email;

        return usuarioRepository.findByEmailWithPlan(email)
        		.map(u -> {
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    
                    // IMPORTANTE: Asegúrate de que u.getRol() no sea null
                    String rolNombre = u.getRol() != null ? u.getRol().name() : "VISOR";
                    
                    // Agregamos el rol con el prefijo que Spring espera
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + rolNombre));
                    
                    System.out.println(">>> ÉXITO: Usuario [" + finalEmail + "] encontrado. Rol: ROLE_" + rolNombre);
                    return (Collection<GrantedAuthority>) authorities;
                })
                .orElseGet(() -> {
                	System.out.println(">>> ERROR: El email [" + finalEmail + "] NO existe en la base de datos.");
                    return List.of(new SimpleGrantedAuthority("ROLE_VISOR"));
                });
        }
}












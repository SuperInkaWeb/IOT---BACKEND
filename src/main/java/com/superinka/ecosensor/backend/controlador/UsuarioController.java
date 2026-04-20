package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.LocalDateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.superinka.ecosensor.backend.dto.CompletarPerfilDTO;
import com.superinka.ecosensor.backend.dto.UsuarioResponseDTO;
import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.modelo.Rol;
import com.superinka.ecosensor.backend.modelo.TipoUsuario;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.servicio.EmailService;
import com.superinka.ecosensor.backend.servicio.EmpresaService;
import com.superinka.ecosensor.backend.servicio.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmpresaService empresaService;
    private final EmailService emailService;

    // 🔥 CRÍTICO: devuelve 404 cuando el usuario es nuevo — NO 500
    // el frontend usa este 404 para redirigir a /completar-perfil
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponseDTO> perfil(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) email = jwt.getSubject();

        return usuarioService.buscarPorEmail(email)
                .map(u -> ResponseEntity.ok(new UsuarioResponseDTO(u)))
                .orElse(ResponseEntity.notFound().build()); // 🔥 404 no 500
    }

    @PostMapping("/completar-perfil")
    public UsuarioResponseDTO completarPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CompletarPerfilDTO dto) {
    	
    	
    	String email = jwt.getClaimAsString("email");
        
        // 🔥 SIN fallback al sub — si no hay email, lanzar error claro
        if (email == null) {
        	throw new RuntimeException("JWT sin email");
        }
        
        Usuario u = usuarioService.buscarPorEmail(email)
                .orElse(Usuario.builder()
                        .email(email)
                        .fechaCreacion(LocalDateTime.now())
                        .passwordHash("AUTH0_USER")
                        .activo(true) // Importante: que nazca activo
                        .build());

        u.setNombre(dto.getNombre());
        u.setTipoUsuario(dto.getTipoUsuario());

        if (email.equalsIgnoreCase("admin@ecosensor.com")) {
            u.setRol(Rol.ADMIN);
            u.setEmpresa(null);
        } 
        // 2. Si no es el dueño, aplicar lógica normal
        else if (dto.getTipoUsuario() == TipoUsuario.EMPRESA) {
            u.setRol(Rol.ADMIN); // Admin de su empresa
            if (dto.getEmpresaId() != null) {
                Empresa empresa = empresaService.obtenerPorId(dto.getEmpresaId());
                u.setEmpresa(empresa);
            }
        } else {
            u.setRol(Rol.VISOR); // Usuario de hogar o visor estándar
            u.setEmpresa(null);
        }
        Usuario usuarioGuardado = usuarioService.guardar(u);

        if (dto.isRecibirAlertasEmail()) {
            // Al ser @Async, el controller sigue de largo sin esperar a Brevo
            emailService.enviarBienvenida(usuarioGuardado.getEmail(), usuarioGuardado.getNombre(), u.getTipoUsuario().toString());
        }

        return new UsuarioResponseDTO(usuarioGuardado);
    }
}
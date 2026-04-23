package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.superinka.ecosensor.backend.dto.CompletarPerfilDTO;
import com.superinka.ecosensor.backend.dto.UsuarioResponseDTO;
import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.modelo.Plan;
import com.superinka.ecosensor.backend.modelo.Rol;
import com.superinka.ecosensor.backend.modelo.TipoUsuario;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.PlanRepository;
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
    private final PlanRepository planRepository;


    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponseDTO> perfil(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null) email = jwt.getSubject();

        return usuarioService.buscarPorEmail(email)
                .map(u -> ResponseEntity.ok(new UsuarioResponseDTO(u)))
                .orElse(ResponseEntity.notFound().build()); // 🔥 404 no 500
    }

    @Transactional
    @PostMapping("/completar-perfil")
    public UsuarioResponseDTO completarPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CompletarPerfilDTO dto) {
    	
    	
    	String email = jwt.getClaimAsString("email");
        
        //SIN fallback al sub — si no hay email, lanzar error claro
        if (email == null) {
        	throw new RuntimeException("JWT sin email");
        }
        

        // Buscar existente o crear nuevo
        Usuario u = usuarioService.buscarPorEmail(email)
                .orElse(Usuario.builder()
                        .email(email)
                        .fechaCreacion(LocalDateTime.now())
                        .passwordHash("AUTH0_USER")
                        .activo(true)
                        .build());
 
        u.setNombre(dto.getNombre());
        u.setTipoUsuario(dto.getTipoUsuario());
        u.setActivo(true);
        u.setRecibirAlertasEmail(dto.isRecibirAlertasEmail());
 
        if (email.equalsIgnoreCase("admin@ecosensor.com")) {
            // Admin del sistema — sin empresa
            u.setRol(Rol.ADMIN);
            u.setEmpresa(null);
        } else {
            // Todos los demás usuarios (EMPRESA u HOGAR) son ADMIN de su propio espacio
            // La empresa se vincula después mediante EmpresaController
            u.setRol(Rol.VISOR);
        }
 
        
        
        if (dto.getTipoUsuario() == TipoUsuario.EMPRESA
                && dto.getNombre() != null
                && !dto.getNombre().isBlank()) {
 
            // Guardar usuario primero para tener ID
            u = usuarioService.guardar(u);
 
            // Solo crear empresa si NO tiene una ya
            if (u.getEmpresa() == null) {
                Plan planBase = planRepository.findById(dto.getPlanId() != null ? dto.getPlanId() : 1L)
                        .orElseThrow(() -> new RuntimeException("Plan base no encontrado en BD"));
 
                Empresa empresa = Empresa.builder()
                        .nombre(dto.getEmpresaNombre())
                        .ruc(dto.getRuc())
                        .activa(true)
                        .creador(u)
                        .plan(planBase)
                        .fechaCreacion(LocalDateTime.now())
                        .build();
 
                Empresa empresaGuardada = empresaService.guardar(empresa);
 
                // 🔥 Vincular empresa al usuario
                u.setEmpresa(empresaGuardada);
                u = usuarioService.guardar(u);
            }
 
        } else if (dto.getTipoUsuario() == TipoUsuario.HOGAR) {
            u.setEmpresa(null);
            u = usuarioService.guardar(u);
        } else {
            u = usuarioService.guardar(u);
        }
        
        Usuario guardado = usuarioService.guardar(u);
 
        // Email de bienvenida — no debe bloquear el registro si falla
        try {
            if (dto.isRecibirAlertasEmail()) {
                emailService.enviarBienvenida(
                    guardado.getEmail(),
                    guardado.getNombre(),
                    guardado.getTipoUsuario() != null ? guardado.getTipoUsuario().toString() : "HOGAR"
                );
            }
        } catch (Exception ignored) {
            System.err.println("Email de bienvenida falló (no crítico)");
        }
 
        return new UsuarioResponseDTO(guardado);
    }
}
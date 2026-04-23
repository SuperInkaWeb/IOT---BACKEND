package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.dto.EmpresaRequest;
import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;
import com.superinka.ecosensor.backend.servicio.EmpresaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class EmpresaController {

    private final EmpresaService empresaService;
    private final UsuarioRepository usuarioRepository;


    
    @PostMapping("/registro")
    public ResponseEntity<?> crear(@RequestBody EmpresaRequest request,@AuthenticationPrincipal Jwt jwt) {
    	try {
            String email = jwt.getClaimAsString("email");
            if (email == null) email = jwt.getSubject();
 
            Empresa empresa = empresaService.guardar(request);
 
            // 🔥 Vincular empresa al usuario autenticado
            String finalEmail = email;
            usuarioRepository.findByEmailWithPlan(email).ifPresent(u -> {
                u.setEmpresa(empresa);
                usuarioRepository.save(u);
            });
 
            return ResponseEntity.ok(Map.of(
                "empresaId",   empresa.getId(),
                "nombre",      empresa.getNombre(),
                "planNombre",  empresa.getPlan() != null ? empresa.getPlan().getNombre() : "Básico"
            ));
 
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Empresa obtener(@PathVariable Long id) {
        return empresaService.obtenerPorId(id);
    }
    
    @GetMapping("/mis-empresas/{usuarioId}")
    public List<Empresa> listarMisEmpresas(@PathVariable Long usuarioId) {
        // Este método lo debes crear en el repositorio: 
        // findByCreadorId(Long usuarioId)
        return empresaService.listarPorCreador(usuarioId);
    }
    
}
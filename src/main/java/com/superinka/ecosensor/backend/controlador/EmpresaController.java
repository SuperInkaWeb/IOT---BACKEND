package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.dto.EmpresaRequest;
import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.servicio.EmpresaService;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class EmpresaController {

    private final EmpresaService empresaService;

    
    @PostMapping
    public Empresa crear(@RequestBody EmpresaRequest request) {
        return empresaService.guardar(request);
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
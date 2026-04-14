package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.superinka.ecosensor.backend.repositorio.*;
import com.superinka.ecosensor.backend.modelo.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final PlanRepository planRepository;

    // Solo ADMIN puede acceder
    // @PreAuthorize("hasRole('ADMIN')")  // activar cuando implementes roles admin

    // ── KPIs GLOBALES ─────────────────────────────────────────────
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        Map<String, Object> kpis = new LinkedHashMap<>();

        kpis.put("totalUsuarios",    usuarioRepository.count());
        kpis.put("totalEmpresas",    empresaRepository.count());
        kpis.put("totalSensores",    sensorRepository.count());
        kpis.put("sensoresActivos",  sensorRepository.countByActivoTrue());
        kpis.put("totalAlertas",     alertaRepository.count());
        kpis.put("alertasSinAtender", alertaRepository.countByAtendida(false));
        kpis.put("suscripcionesActivas",
                suscripcionRepository.countByEstado("ACTIVA"));

        return ResponseEntity.ok(kpis);
    }

    // ── TODOS LOS USUARIOS ────────────────────────────────────────
    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> getUsuarios() {
        List<Map<String, Object>> lista = usuarioRepository.findAll()
                .stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          u.getId());
                    m.put("nombre",      u.getNombre());
                    m.put("email",       u.getEmail());
                    m.put("rol",         u.getRol() != null ? u.getRol().name() : null);
                    m.put("tipoUsuario", u.getTipoUsuario() != null ? u.getTipoUsuario().name() : null);
                    m.put("empresa",     u.getEmpresa() != null ? u.getEmpresa().getNombre() : null);
                    m.put("activo",      u.getActivo());
                    m.put("fechaCreacion", u.getFechaCreacion());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ── TODAS LAS EMPRESAS ────────────────────────────────────────
    @GetMapping("/empresas")
    public ResponseEntity<List<Map<String, Object>>> getEmpresas() {
        List<Map<String, Object>> lista = empresaRepository.findAll()
                .stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",            e.getId());
                    m.put("nombre",        e.getNombre());
                    m.put("ruc",           e.getRuc());
                    m.put("email",         e.getEmailContacto());
                    m.put("activa",        e.getActiva());
                    m.put("plan",          e.getPlan() != null ? e.getPlan().getNombre() : null);
                    m.put("totalSensores", sensorRepository.countByEmpresaId(e.getId()));
                    m.put("fechaCreacion", e.getFechaCreacion());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ── TODAS LAS SUSCRIPCIONES ───────────────────────────────────
    @GetMapping("/suscripciones")
    public ResponseEntity<List<Map<String, Object>>> getSuscripciones() {
        List<Map<String, Object>> lista = suscripcionRepository.findAll()
                .stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          s.getId());
                    m.put("empresa",     s.getEmpresa() != null ? s.getEmpresa().getNombre() : null);
                    m.put("plan",        s.getPlan() != null ? s.getPlan().getNombre() : null);
                    m.put("precio",      s.getPlan() != null ? s.getPlan().getPrecioMensual() : null);
                    m.put("estado",      s.getEstado());
                    m.put("estadoPago",  s.getEstadoPago());
                    m.put("fechaInicio", s.getFechaInicio());
                    m.put("fechaFin",    s.getFechaFin());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ── ALERTAS GLOBALES RECIENTES ────────────────────────────────
    @GetMapping("/alertas")
    public ResponseEntity<List<Map<String, Object>>> getAlertas() {
        List<Map<String, Object>> lista = alertaRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    if (a.getFecha() == null) return 1;
                    if (b.getFecha() == null) return -1;
                    return b.getFecha().compareTo(a.getFecha());
                })
                .limit(50) // últimas 50
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          a.getId());
                    m.put("sensor",      a.getSensor() != null ? a.getSensor().getDeviceId() : null);
                    m.put("empresa",     a.getSensor() != null && a.getSensor().getEmpresa() != null
                            ? a.getSensor().getEmpresa().getNombre() : "Hogar");
                    m.put("tipoMetrica", a.getTipoMetrica());
                    m.put("mensaje",     a.getMensaje());
                    m.put("nivel",       a.getNivel());
                    m.put("valor",       a.getValor());
                    m.put("atendida",    a.getAtendida());
                    m.put("fecha",       a.getFecha());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ── PLANES DISPONIBLES ────────────────────────────────────────
    @GetMapping("/planes")
    public ResponseEntity<List<Plan>> getPlanes() {
        return ResponseEntity.ok(planRepository.findAll());
    }

    // ── DESACTIVAR USUARIO ────────────────────────────────────────
    @PutMapping("/usuarios/{id}/desactivar")
    public ResponseEntity<String> desactivarUsuario(@PathVariable Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
        });
        return ResponseEntity.ok("Usuario desactivado");
    }
    
    
    @PutMapping("/usuarios/{id}/activar")
    public ResponseEntity<String> activarUsuario(@PathVariable Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(true);
            usuarioRepository.save(u);
        });
        return ResponseEntity.ok("Usuario activado");
    }

    // ── ACTIVAR/DESACTIVAR EMPRESA ────────────────────────────────
    @PutMapping("/empresas/{id}/desactivar")
    public ResponseEntity<String> desactivarEmpresa(@PathVariable Long id) {
        empresaRepository.findById(id).ifPresent(e -> {
            e.setActiva(false);
            empresaRepository.save(e);
        });
        return ResponseEntity.ok("Empresa desactivada");
    }

    @PutMapping("/empresas/{id}/activar")
    public ResponseEntity<String> activarEmpresa(@PathVariable Long id) {
        empresaRepository.findById(id).ifPresent(e -> {
            e.setActiva(true);
            empresaRepository.save(e);
        });
        return ResponseEntity.ok("Empresa activada");
    }

    // ── SUSPENDER SUSCRIPCIÓN ─────────────────────────────────────
    @PutMapping("/suscripciones/{id}/suspender")
    public ResponseEntity<String> suspenderSuscripcion(@PathVariable Long id) {
        suscripcionRepository.findById(id).ifPresent(s -> {
            s.setEstado("CANCELADA");
            s.setEstadoPago("FALLIDO");
            suscripcionRepository.save(s);
        });
        return ResponseEntity.ok("Suscripción suspendida");
    }

    @PutMapping("/suscripciones/{id}/activar")
    public ResponseEntity<String> activarSuscripcion(@PathVariable Long id) {
        suscripcionRepository.findById(id).ifPresent(s -> {
            s.setEstado("ACTIVA");
            s.setEstadoPago("PAGADO");
            suscripcionRepository.save(s);
        });
        return ResponseEntity.ok("Suscripción activada");
    }
    
}
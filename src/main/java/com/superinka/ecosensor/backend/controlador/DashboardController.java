package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.superinka.ecosensor.backend.dto.DashboardResponse;
import com.superinka.ecosensor.backend.dto.SensorResumenDTO;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.servicio.DashboardService;
import com.superinka.ecosensor.backend.servicio.ReporteService;
import com.superinka.ecosensor.backend.servicio.UsuarioService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UsuarioService usuarioService;
    private final ReporteService reporteService;

    @GetMapping("/empresa/{empresaId}")
    public DashboardResponse obtenerDashboard(@PathVariable Long empresaId, @AuthenticationPrincipal Jwt jwt) {
    	validarPertenenciaEmpresa(empresaId, jwt);
        return dashboardService.obtenerDashboardEmpresa(empresaId);
    }
    
    @GetMapping("/empresa/{empresaId}/sensores")
    public List<SensorResumenDTO> listarSensores(@PathVariable Long empresaId, @AuthenticationPrincipal Jwt jwt) {
        // 💡 Lógica para Usuarios 'Hogar' (empresaId 0)
        if (empresaId == null || empresaId == 0) {
            String email = extraerEmail(jwt);
            System.out.println("🏠 Modo Hogar detectado para: " + email);
            return dashboardService.listarSensoresPorUsuario(email);
        }
        
        validarPertenenciaEmpresa(empresaId, jwt);
        return dashboardService.listarSensores(empresaId);
    }
    
    @GetMapping("/empresa/{empresaId}/reporte-pdf")
    public ResponseEntity<byte[]> descargarReporte(@PathVariable Long empresaId, @AuthenticationPrincipal Jwt jwt) {
        // 2. Validamos seguridad igual que en los otros métodos
        validarPertenenciaEmpresa(empresaId, jwt);
        
        // 3. Obtenemos los datos y generamos el PDF
        DashboardResponse data = dashboardService.obtenerDashboardEmpresa(empresaId);
        byte[] pdf = reporteService.generarReporteEmpresa(data, "Empresa ID: " + empresaId);

        // 4. Retornamos el archivo con los headers correctos
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Reporte_Ambiental.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
    
    private String extraerEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return (email != null) ? email : jwt.getSubject();
    }
    
    @GetMapping("/sensor/{sensorId}")
    public DashboardResponse obtenerPorSensor(@PathVariable Long sensorId, @AuthenticationPrincipal Jwt jwt) {
        // Validar que el sensor pertenezca a la empresa del usuario
        String email = extraerEmail(jwt);
        dashboardService.validarAccesoSensor(email, sensorId); 
        return dashboardService.obtenerDashboardSensor(sensorId);
    }
    
    
    private void validarPertenenciaEmpresa(Long empresaIdRequerida, Jwt jwt) {
    		String email = extraerEmail(jwt);
        
        Usuario usuario = usuarioService.buscarPorEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new AccessDeniedException("Usuario no registrado en DB: " + email));

        if ("ADMIN".equals(usuario.getRol().name())) return;

        if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresaIdRequerida)) {
            throw new AccessDeniedException("Acceso bloqueado a empresa ID: " + empresaIdRequerida);
        }
    }
}









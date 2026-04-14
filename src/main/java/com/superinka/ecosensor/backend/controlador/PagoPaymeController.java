package com.superinka.ecosensor.backend.controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.superinka.ecosensor.backend.modelo.*;
import com.superinka.ecosensor.backend.repositorio.*;
import com.superinka.ecosensor.backend.servicio.UsuarioService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://*.netlify.app"}, allowCredentials = "true")

public class PagoPaymeController {

    private final SuscripcionRepository suscripcionRepository;
    private final EmpresaRepository empresaRepository;
    private final PlanRepository planRepository;
    private final UsuarioService usuarioService;

    // 🔥 estas credenciales las da Pay-me al registrarte
    @Value("${payme.merchant-id}")
    private String merchantId;

    @Value("${payme.access-key}")
    private String accessKey;

    @Value("${payme.api-url}")
    private String paymeApiUrl;

    // ── PASO 1: Frontend llama esto para iniciar el pago ──────────
    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciarPago(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal Jwt jwt) {

        Long planId    = Long.valueOf(body.get("planId").toString());
        Long empresaId = Long.valueOf(body.get("empresaId").toString());

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // crear suscripción en estado PENDIENTE
        Suscripcion suscripcion = Suscripcion.builder()
                .empresa(empresa)
                .plan(plan)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(1))
                .estado("PENDIENTE")        // 🔥 no ACTIVA todavía
                .estadoPago("PENDIENTE")
                .moneda("PEN")
                .build();

        suscripcion = suscripcionRepository.save(suscripcion);
        final Long suscripcionId = suscripcion.getId();

        // llamar a Pay-me para generar el token de pago
        // 🔥 cuando tengas las credenciales reales, descomenta esto:
        /*
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessKey);

        Map<String, Object> paymeBody = new HashMap<>();
        paymeBody.put("merchantId", merchantId);
        paymeBody.put("amount", plan.getPrecioMensual());
        paymeBody.put("currency", "PEN");
        paymeBody.put("orderId", suscripcionId.toString());
        paymeBody.put("description", "Plan " + plan.getNombre() + " - EcoSensor");
        paymeBody.put("urlReturn", ""https:////iot---frontend.netlify.app/suscripcion/confirmado");
        paymeBody.put("urlNotify", "https://iot-backend.onrender.com/api/pagos/webhook");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(paymeBody, headers);
        ResponseEntity<Map> response = rest.postForEntity(paymeApiUrl + "/token", request, Map.class);
        String checkoutUrl = (String) response.getBody().get("checkoutUrl");
        */

        // 🔥 por ahora (sandbox) devolver datos simulados
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("suscripcionId", suscripcionId);
        resultado.put("planNombre", plan.getNombre());
        resultado.put("monto", plan.getPrecioMensual());
        resultado.put("moneda", "PEN");
        // cuando tengas credenciales: resultado.put("checkoutUrl", checkoutUrl);
        resultado.put("checkoutUrl", "https://sandbox.pay-me.com/checkout?order=" + suscripcionId);

        return ResponseEntity.ok(resultado);
    }

    // ── PASO 2: Pay-me llama esto cuando confirma el pago ─────────
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody Map<String, Object> body) {

        String orderId = body.get("orderId") != null
                ? body.get("orderId").toString() : null;
        String estado  = body.get("estado") != null
                ? body.get("estado").toString() : null;
        String paymeTxId = body.get("transactionId") != null
                ? body.get("transactionId").toString() : null;

        if (orderId == null || estado == null) {
            return ResponseEntity.badRequest().body("Datos incompletos");
        }

        Long suscripcionId = Long.valueOf(orderId);

        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElse(null);

        if (suscripcion == null) {
            return ResponseEntity.notFound().build();
        }

        if ("APPROVED".equals(estado) || "PAGADO".equals(estado)) {
            // 🔥 pago confirmado → activar suscripción
            suscripcion.setEstado("ACTIVA");
            suscripcion.setEstadoPago("PAGADO");
            suscripcion.setPaymeOrderId(paymeTxId);
            suscripcion.setFechaInicio(LocalDate.now());
            suscripcion.setFechaFin(LocalDate.now().plusMonths(1));
        } else {
            // pago rechazado o cancelado
            suscripcion.setEstado("CANCELADA");
            suscripcion.setEstadoPago("RECHAZADO");
        }

        suscripcionRepository.save(suscripcion);
        return ResponseEntity.ok("OK");
    }
}
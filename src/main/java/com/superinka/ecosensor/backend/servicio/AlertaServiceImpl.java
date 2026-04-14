package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.superinka.ecosensor.backend.modelo.*;
import com.superinka.ecosensor.backend.repositorio.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public List<Alerta> listarTodas() {
        return alertaRepository.findAll();
    }

    @Override
    public List<Alerta> listarPorSensor(Long sensorId) {
        if (sensorId == null) throw new IllegalArgumentException("sensorId no puede ser null");
        return alertaRepository.findBySensorId(sensorId);
    }

    @Override
    public List<Alerta> listarNoAtendidas() {
        return alertaRepository.findByAtendidaFalse();
    }

    @Override
    public List<Alerta> listarPorUsuarioEmail(String email) {
    	
    	return usuarioRepository.findByEmailWithPlan(email)
                .map(usuario -> {
                    // Si el usuario es tipo EMPRESA, buscamos por el ID de su empresa
                    if (usuario.getTipoUsuario() != null && "EMPRESA".equals(usuario.getTipoUsuario().name())) {
                        return alertaRepository.findBySensorEmpresaId(usuario.getEmpresa().getId());
                    } 
                    // Si es usuario normal (o cualquier otro), por su ID de usuario
                    else {
                        return alertaRepository.findBySensorUsuarioId(usuario.getId());
                    }
                })
                // Si el email no existe en la BD, devolvemos una lista vacía en lugar de romper el servidor
                .orElse(java.util.Collections.emptyList());
    }

    @Override
    public Alerta marcarComoAtendida(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser null");
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada"));
        alerta.setAtendida(true);
        return alertaRepository.save(alerta);
    }

    // ── REGLAS DE ALERTA POR TIPO ─────────────────────────────────

    public void revisarLectura(LecturaSensor lectura) {
        if (lectura == null || lectura.getSensor() == null) return;

        TipoMetrica tipo  = lectura.getTipoMetrica();
        Double      valor = lectura.getValor();
        Sensor      sensor = lectura.getSensor();

        if (tipo == null || valor == null) return;

        switch (tipo) {

            // ── AIRE ──────────────────────────────────────────────
            case PM25:
                if (valor > 150) crearAlerta(sensor, lectura, tipo.name(), valor, "Calidad del aire peligrosa", "ALTO");
                else if (valor > 50) crearAlerta(sensor, lectura, tipo.name(), valor, "Calidad del aire moderada", "MEDIO");
                break;

            case CO2:
                if (valor > 1000) crearAlerta(sensor, lectura, tipo.name(), valor, "¡Ventilar habitación! CO2 alto", "ALTO");
                else if (valor > 700) crearAlerta(sensor, lectura, tipo.name(), valor, "CO2 elevado", "MEDIO");
                break;

            case TEMPERATURA:
                if (valor > 30) crearAlerta(sensor, lectura, tipo.name(), valor, "Temperatura muy alta", "ALTO");
                else if (valor < 15) crearAlerta(sensor, lectura, tipo.name(), valor, "Temperatura muy baja", "ALTO");
                else if (valor > 26) crearAlerta(sensor, lectura, tipo.name(), valor, "Temperatura elevada", "MEDIO");
                break;

            case HUMEDAD:
                if (valor > 70) crearAlerta(sensor, lectura, tipo.name(), valor, "Humedad muy alta — riesgo de moho", "ALTO");
                else if (valor < 30) crearAlerta(sensor, lectura, tipo.name(), valor, "Humedad muy baja", "ALTO");
                else if (valor > 60) crearAlerta(sensor, lectura, tipo.name(), valor, "Humedad elevada", "MEDIO");
                break;

            case TVOC:
                if (valor > 1000) crearAlerta(sensor, lectura, tipo.name(), valor, "Concentración de VOC peligrosa", "ALTO");
                else if (valor > 500) crearAlerta(sensor, lectura, tipo.name(), valor, "VOC elevado — ventilar", "MEDIO");
                break;

            case RUIDO:
                if (valor > 85) crearAlerta(sensor, lectura, tipo.name(), valor, "Nivel de ruido peligroso", "ALTO");
                else if (valor > 70) crearAlerta(sensor, lectura, tipo.name(), valor, "Ruido elevado", "MEDIO");
                break;

            // ── AGUA ──────────────────────────────────────────────
            case PH:
                if (valor < 6.0 || valor > 9.0) crearAlerta(sensor, lectura, tipo.name(), valor, "¡Calidad del agua crítica! pH fuera de rango", "ALTO");
                else if (valor < 6.5 || valor > 8.5) crearAlerta(sensor, lectura, tipo.name(), valor, "pH fuera del rango potable", "MEDIO");
                break;

            case TURBIDEZ:
                // NTU — agua potable < 5 NTU
                if (valor > 10) crearAlerta(sensor, lectura, tipo.name(), valor, "¡Agua turbia! Turbidez muy alta", "ALTO");
                else if (valor > 5) crearAlerta(sensor, lectura, tipo.name(), valor, "Agua turbia — verificar filtros", "MEDIO");
                break;

            case TDS:
                // ppm — agua potable < 500 ppm
                if (valor > 1000) crearAlerta(sensor, lectura, tipo.name(), valor, "¡Alta concentración de sólidos! Agua no potable", "ALTO");
                else if (valor > 500) crearAlerta(sensor, lectura, tipo.name(), valor, "TDS elevado — revisar agua", "MEDIO");
                break;

            case CONDUCTIVIDAD:
                // µS/cm
                if (valor > 2000) crearAlerta(sensor, lectura, tipo.name(), valor, "Conductividad muy alta — posible contaminación", "ALTO");
                else if (valor > 1500) crearAlerta(sensor, lectura, tipo.name(), valor, "Conductividad elevada", "MEDIO");
                break;

            // ── ENERGÍA ───────────────────────────────────────────
            case ENERGIA:
                if (valor > 10) crearAlerta(sensor, lectura, tipo.name(), valor, "¡Consumo eléctrico crítico!", "ALTO");
                else if (valor > 5) crearAlerta(sensor, lectura, tipo.name(), valor, "Consumo energético alto", "MEDIO");
                break;

            case VOLTAJE:
                if (valor < 200 || valor > 250) crearAlerta(sensor, lectura, tipo.name(), valor, "Voltaje fuera de rango — riesgo eléctrico", "ALTO");
                else if (valor < 210 || valor > 240) crearAlerta(sensor, lectura, tipo.name(), valor, "Voltaje irregular", "MEDIO");
                break;

            case CORRIENTE:
                if (valor > 25) crearAlerta(sensor, lectura, tipo.name(), valor, "¡Sobrecarga eléctrica!", "ALTO");
                else if (valor > 20) crearAlerta(sensor, lectura, tipo.name(), valor, "Corriente alta — revisar circuito", "MEDIO");
                break;

            default:
                if (valor > 100) crearAlerta(sensor, lectura, tipo.name(), valor, "Valor fuera de rango", "MEDIO");
                break;
        }
    }

    // ── CREAR ALERTA SIN DUPLICADOS ───────────────────────────────

    private void crearAlerta(Sensor sensor, LecturaSensor lectura,
                             String tipoMetrica, Double valor,
                             String mensaje, String nivel) {

        boolean existe = alertaRepository.existsBySensorIdAndTipoMetricaAndMensajeAndFechaBetween(
                sensor.getId(),
                tipoMetrica,
                mensaje,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now()
        );

        if (existe) return;

        Alerta alerta = Alerta.builder()
                .sensor(sensor)
                .lectura(lectura)
                .tipoMetrica(tipoMetrica)
                .valor(valor)
                .mensaje(mensaje)
                .nivel(nivel)
                .atendida(false)
                .fecha(LocalDateTime.now())
                .build();

        alertaRepository.save(alerta);
    }
}
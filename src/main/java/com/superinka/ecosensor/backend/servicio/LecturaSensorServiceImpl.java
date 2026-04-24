package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.superinka.ecosensor.backend.dto.LecturaRequest;
import com.superinka.ecosensor.backend.dto.LecturaResponse;
import com.superinka.ecosensor.backend.modelo.*;
import com.superinka.ecosensor.backend.repositorio.*;
import com.superinka.ecosensor.backend.ml.AnomalyDetectionService;
import com.superinka.ecosensor.backend.ml.MlTrainingService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LecturaSensorServiceImpl implements LecturaSensorService {

    private final LecturaSensorRepository lecturaRepository;
    private final ConfiguracionMetricaRepository configuracionRepository;
    private final AlertaRepository alertaRepository;
    private final SensorRepository sensorRepository;
    private final UsuarioRepository usuarioRepository;

    private final AnomalyDetectionService anomalyService;
    private final MlTrainingService mlTrainingService;

    //inyectamos EmailService para alertas automáticas
    private final EmailService emailService;

    @Override
    public LecturaResponse registrarDesdeSensor(LecturaRequest request) {

        if (request.getDeviceId() == null)
            throw new IllegalArgumentException("El deviceId no puede ser null");
        if (request.getTipoMetrica() == null)
            throw new IllegalArgumentException("El tipoMetrica no puede ser null");
        if (request.getValor() == null)
            throw new IllegalArgumentException("El valor no puede ser null");

        Sensor sensor = sensorRepository
                .findByDeviceId(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("Sensor no encontrado"));

        if (sensor.getActivo() == null || !sensor.getActivo())
            throw new IllegalArgumentException("El sensor está inactivo");

        TipoMetrica tipo;
        try {
            tipo = TipoMetrica.valueOf(request.getTipoMetrica().trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo de métrica inválido: " + request.getTipoMetrica());
        }

        var configOpt = configuracionRepository
                .findBySensorIdAndTipoMetrica(sensor.getId(), tipo);

        if (configOpt.isEmpty())
            throw new IllegalArgumentException("La métrica no está configurada para este sensor");

        var config = configOpt.get();

        LecturaSensor lectura = LecturaSensor.builder()
                .sensor(sensor)
                .tipoMetrica(tipo)
                .valor(request.getValor())
                .temperatura(request.getTemperatura())
                .humedad(request.getHumedad())
                .fecha(LocalDateTime.now())
                .build();

        LecturaSensor nueva = lecturaRepository.save(lectura);

        // ── VALIDAR RANGO ──────────────────────────────────────────
        Double valor = nueva.getValor();
        boolean fueraDeRango =
                (config.getRangoMin() != null && valor < config.getRangoMin()) ||
                (config.getRangoMax() != null && valor > config.getRangoMax());

        if (fueraDeRango) {
            Alerta alerta = Alerta.builder()
                    .sensor(sensor)
                    .lectura(nueva)
                    .tipoMetrica(request.getTipoMetrica())
                    .valor(valor)
                    .mensaje("Valor fuera de rango")
                    .nivel("ALTO")
                    .atendida(false)
                    .fecha(LocalDateTime.now())
                    .build();

            Alerta alertaGuardada = alertaRepository.save(alerta);

            //enviar email automático para alertas ALTO
            enviarEmailAlerta(alertaGuardada, sensor);
        }

        // ── MACHINE LEARNING ───────────────────────────────────────
        String ownerId = (sensor.getEmpresa() != null)
                ? sensor.getEmpresa().getId().toString()
                : sensor.getUsuario().getId().toString();

        String key = ownerId + "_" + tipo.name();

        boolean esAnomalia = anomalyService.esAnomalia(
                key, nueva.getValor(), nueva.getTemperatura(), nueva.getHumedad()
        );

        if (esAnomalia) {
            nueva.setAnomaliaDetectada(true);
            lecturaRepository.save(nueva);

            Alerta alerta = Alerta.builder()
                    .sensor(sensor)
                    .lectura(nueva)
                    .tipoMetrica(request.getTipoMetrica())
                    .valor(nueva.getValor())
                    .mensaje("Anomalía detectada por ML")
                    .nivel("CRITICO")
                    .atendida(false)
                    .fecha(LocalDateTime.now())
                    .build();

            Alerta alertaGuardada = alertaRepository.save(alerta);

            //enviar email automático para anomalías CRITICO
            enviarEmailAlerta(alertaGuardada, sensor);
        }

        return LecturaResponse.builder()
                .id(nueva.getId())
                .deviceId(nueva.getSensor().getDeviceId())
                .tipoMetrica(nueva.getTipoMetrica().name())
                .valor(nueva.getValor())
                .temperatura(nueva.getTemperatura())
                .humedad(nueva.getHumedad())
                .fecha(nueva.getFecha())
                .build();
    }

    // ── ENVIAR EMAIL DE ALERTA ─────────────────────────────────────
    private void enviarEmailAlerta(Alerta alerta, Sensor sensor) {
        try {
            // intentar obtener email del dueño del sensor
            String email = null;
            String nombre = "Usuario";

            if (sensor.getUsuario() != null) {
                email  = sensor.getUsuario().getEmail();
                nombre = sensor.getUsuario().getNombre();
            } else if (sensor.getEmpresa() != null) {
                email  = sensor.getEmpresa().getEmailContacto();
                nombre = sensor.getEmpresa().getNombre();
            }

            if (email != null && !email.isBlank()) {
                emailService.enviarAlertaEmail(alerta, email, nombre);
            }
        } catch (Exception e) {
            // no bloquear el flujo principal si el email falla
            System.err.println("Error enviando email de alerta: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void entrenarModelo() {
        mlTrainingService.entrenarModeloGlobal();
    }

    @Override
    public List<LecturaSensor> listarPorSensor(Long sensorId) {
        return lecturaRepository.findBySensorId(sensorId);
    }

    @Override
    public List<LecturaSensor> listarTodas() {
        return lecturaRepository.findAll();
    }
}
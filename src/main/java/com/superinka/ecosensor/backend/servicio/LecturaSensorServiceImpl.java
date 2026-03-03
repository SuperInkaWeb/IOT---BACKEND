package com.superinka.ecosensor.backend.servicio;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.dto.LecturaRequest;
import com.superinka.ecosensor.backend.modelo.*;
import com.superinka.ecosensor.backend.repositorio.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LecturaSensorServiceImpl implements LecturaSensorService {

    private final LecturaSensorRepository lecturaRepository;
    private final ConfiguracionMetricaRepository configuracionRepository;
    private final AlertaRepository alertaRepository;
    private final SensorRepository sensorRepository;

    // 🔹 Registrar lectura desde IoT
    @Override
    public LecturaSensor registrarDesdeSensor(LecturaRequest request) {

        Sensor sensor = sensorRepository.findById(request.getSensorId())
                .orElseThrow(() -> new RuntimeException("Sensor no encontrado"));

        LecturaSensor lectura = LecturaSensor.builder()
                .sensor(sensor)
                .tipoMetrica(request.getTipoMetrica())
                .valor(request.getValor())
                .build();

        LecturaSensor nueva = lecturaRepository.save(lectura);

        configuracionRepository
                .findBySensorIdAndTipoMetrica(sensor.getId(), request.getTipoMetrica())
                .ifPresent(config -> {

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
                                .build();

                        alertaRepository.save(alerta);
                    }
                });

        return nueva;
    }

    // 🔹 Listar lecturas por sensor
    @Override
    public List<LecturaSensor> listarPorSensor(Long sensorId) {
        return lecturaRepository.findBySensorId(sensorId);
    }

    // 🔹 Listar todas las lecturas
    @Override
    public List<LecturaSensor> listarTodas() {
        return lecturaRepository.findAll();
    }
}
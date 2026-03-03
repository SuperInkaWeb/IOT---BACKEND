package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.modelo.Alerta;
import com.superinka.ecosensor.backend.repositorio.AlertaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaRepository alertaRepository;

    @Override
    public List<Alerta> listarTodas() {
        return alertaRepository.findAll();
    }

    @Override
    public List<Alerta> listarPorSensor(Long sensorId) {
        return alertaRepository.findBySensorId(sensorId);
    }

    @Override
    public List<Alerta> listarNoAtendidas() {
        return alertaRepository.findByAtendidaFalse();
    }

    @Override
    public Alerta marcarComoAtendida(Long id) {

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));

        alerta.setAtendida(true);

        return alertaRepository.save(alerta);
    }
}
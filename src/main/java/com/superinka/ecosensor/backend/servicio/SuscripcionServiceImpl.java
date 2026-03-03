package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.modelo.*;
import com.superinka.ecosensor.backend.repositorio.*;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final EmpresaRepository empresaRepository;
    private final PlanRepository planRepository;

    @Override
    public Suscripcion crearSuscripcion(Long empresaId, Long planId) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        // Verificar si ya tiene suscripción activa
        suscripcionRepository.findByEmpresaIdAndEstado(empresaId, "ACTIVA")
                .ifPresent(s -> {
                    throw new RuntimeException("La empresa ya tiene una suscripción activa");
                });

        Suscripcion suscripcion = Suscripcion.builder()
                .empresa(empresa)
                .plan(plan)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(1))
                .estado("ACTIVA")
                .build();

        return suscripcionRepository.save(suscripcion);
    }

    @Override
    public List<Suscripcion> listarPorEmpresa(Long empresaId) {
        return suscripcionRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Suscripcion cancelarSuscripcion(Long suscripcionId) {

        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));

        suscripcion.setEstado("CANCELADA");

        return suscripcionRepository.save(suscripcion);
    }

    @Override
    public Suscripcion obtenerPorId(Long id) {
        return suscripcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
    }
}
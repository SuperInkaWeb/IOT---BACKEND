package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.dto.SuscripcionResponse;
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
    private final UsuarioRepository usuarioRepository;
    
    private SuscripcionResponse toResponse(Suscripcion sus) {
        return SuscripcionResponse.builder()
                .id(sus.getId())
                .empresaId(sus.getEmpresa() != null ? sus.getEmpresa().getId() : null)
                .planId(sus.getPlan().getId())
                .fechaInicio(sus.getFechaInicio())
                .fechaFin(sus.getFechaFin())
                .estado(sus.getEstado()) //  String
                .build();
    }
    
    @Override
    public SuscripcionResponse crearSuscripcion(Long empresaId, Long planId) {
    	


        if (planId == null) {
            throw new IllegalArgumentException("El planId no puede ser null");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        Empresa empresa = null;
        if (planId != 1) { 
            if (empresaId == null) {
                throw new IllegalArgumentException("Este plan requiere una empresa asociada");
            }
            empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            
            // Verificar si la empresa ya tiene suscripción activa
            if (suscripcionRepository.findByEmpresaIdAndEstado(empresaId, "ACTIVA").isPresent()) {
                throw new IllegalStateException("La empresa ya tiene una suscripción activa");
            }
        }
        
        // Verificar si ya tiene suscripción activa
        boolean tieneActiva = suscripcionRepository
                .findByEmpresaIdAndEstado(empresaId, "ACTIVA")
                .isPresent();

        if (tieneActiva) {
            throw new IllegalStateException("La empresa ya tiene una suscripción activa");
        }
        
        //crear suscripcion
        Suscripcion suscripcion = Suscripcion.builder()
                .empresa(empresa)
                .plan(plan)
                .fechaInicio(LocalDate.now())
                .fechaFin(LocalDate.now().plusMonths(1))
                .estado("ACTIVA")
                .build();

        Suscripcion saved = suscripcionRepository.save(suscripcion);
        return toResponse(saved);
    }

    @Override
    public List<SuscripcionResponse> listarPorEmpresa(Long empresaId) {
    	if (empresaId == null) {
            throw new IllegalArgumentException("El empresaId no puede ser null");
        }
    	
        return suscripcionRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SuscripcionResponse cancelarSuscripcion(Long suscripcionId) {
    	
    	
    	Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada"));
        
        if ("CANCELADA".equals(suscripcion.getEstado())) {
            throw new IllegalArgumentException("La suscripción ya está cancelada");
        }

        suscripcion.setEstado("CANCELADA");

        Suscripcion saved = suscripcionRepository.save(suscripcion);
        return toResponse(saved);
    }

    @Override
    public SuscripcionResponse obtenerPorId(Long id) {
    	

    	
    	Suscripcion suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Suscripción no encontrada"));
    	
        return toResponse(suscripcion);
        
    }

	@Override
	public List<SuscripcionResponse> listarPorUsuario(Long usuarioId) {
		if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId no puede ser null");
        }
        return suscripcionRepository.findByEmpresaCreadorId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
	}
}
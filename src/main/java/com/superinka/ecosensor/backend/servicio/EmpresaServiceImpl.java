package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.dto.EmpresaRequest;
import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.modelo.Plan;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.EmpresaRepository;
import com.superinka.ecosensor.backend.repositorio.PlanRepository;
import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PlanRepository planRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Empresa guardar(EmpresaRequest request) {
    	Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
    	
    	
    	Usuario creador = usuarioRepository.findById(request.getCreadorId())
                .orElseThrow(() -> new RuntimeException("Usuario creador no encontrado"));
    	

        Empresa empresa = Empresa.builder()
                .nombre(request.getNombre())
                .ruc(request.getRuc())
                .emailContacto(request.getEmailContacto())
                .creador(creador)
                .plan(plan)
                .activa(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return empresaRepository.save(empresa);
    }
    
    @Override
    public List<Empresa> listarPorCreador(Long usuarioId) {
        return empresaRepository.findByCreadorId(usuarioId);
    }

    @Override
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    @Override
    public Empresa obtenerPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
    }
}
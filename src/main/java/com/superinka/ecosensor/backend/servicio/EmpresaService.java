package com.superinka.ecosensor.backend.servicio;

import java.util.List;

import com.superinka.ecosensor.backend.dto.EmpresaRequest;
import com.superinka.ecosensor.backend.modelo.Empresa;

public interface EmpresaService {

    Empresa guardar(EmpresaRequest request);

    List<Empresa> listarTodas();
    
    Empresa guardar(Empresa empresa);

    Empresa obtenerPorId(Long id);
    
    List<Empresa> listarPorCreador(Long usuarioId);
}
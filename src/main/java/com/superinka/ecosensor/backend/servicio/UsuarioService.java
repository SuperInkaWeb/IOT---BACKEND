package com.superinka.ecosensor.backend.servicio;


import java.util.List;
import java.util.Optional;

import com.superinka.ecosensor.backend.modelo.Usuario;

public interface UsuarioService {

    Usuario guardar(Usuario usuario);

    List<Usuario> listarPorEmpresa(Long empresaId);

    Optional<Usuario> buscarPorEmail(String email);

    Usuario obtenerPorId(Long id);
}
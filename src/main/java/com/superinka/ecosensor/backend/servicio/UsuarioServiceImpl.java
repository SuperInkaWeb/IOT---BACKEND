package com.superinka.ecosensor.backend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.dto.UsuarioResponseDTO;
import com.superinka.ecosensor.backend.modelo.Rol;
import com.superinka.ecosensor.backend.modelo.TipoUsuario;
import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.repositorio.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    @Override
    public Usuario guardar(Usuario usuario) {
        // IMPORTANTE: Aseguramos que esté activo antes de guardar
        usuario.setActivo(true); 
        
        // Spring Boot detecta automáticamente si es nuevo o existente
        return usuarioRepository.save(usuario); 
    }
    
    
    @Override // Añade esto a tu interfaz UsuarioService también
    public Usuario completarPerfil(UsuarioResponseDTO dto) {
        // 1. Buscamos el usuario que Auth0 ya creó (o debería existir)
        Usuario usuario = usuarioRepository.findByEmailWithPlan(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no registrado en la pre-carga"));
        
        usuario.setNombre(dto.getNombre());
        if (dto.getTipoUsuario() != null) {
            usuario.setTipoUsuario(TipoUsuario.valueOf(dto.getTipoUsuario()));
        }
        usuario.setActivo(true); // Siempre activo al completar perfil

        // 🛡️ Lógica de Super Admin (Dueño del sistema)
        if (dto.getEmail().equalsIgnoreCase("admin@ecosensor.com")) { 
            usuario.setRol(Rol.ADMIN);
            usuario.setEmpresa(null); // Tú no eres de una empresa, eres el dueño de TODO
        } else {
            // Lógica de Cliente
            usuario.setRol(Rol.ADMIN); // Es admin de su propio Hogar o Empresa
            if (dto.getEmpresaId() != null) {
                // Aquí deberías inyectar EmpresaRepository para buscarla y setearla
                // empresaRepository.findById(dto.getEmpresaId()).ifPresent(usuario::setEmpresa);
            }
        }
        
        return usuarioRepository.save(usuario);
    }

    
    @Override
    public Usuario obtenerUsuarioActual(Jwt jwt) {
        // Intentamos sacar el email de varias formas comunes en Auth0
        String email = jwt.getClaimAsString("email");
        
        if (email == null) {
            // Si no está como 'email', buscamos en el 'subject' o en claims personalizados
            email = jwt.getSubject(); 
        }

        // Buscamos en tu repositorio
        return buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("No se encontró al usuario con identificación: " + jwt.getSubject()));
    }

    @Override
    public List<Usuario> listarPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailWithPlan(email);
    }

    @Override
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
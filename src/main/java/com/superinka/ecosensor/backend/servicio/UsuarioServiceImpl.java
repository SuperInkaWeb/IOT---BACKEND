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

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario guardar(Usuario usuario) {
    	    return usuarioRepository.findByEmailWithPlan(usuario.getEmail())
    	            .map(u -> {
    	                // Si existe: actualizamos el objeto recuperado (u) con los datos nuevos
    	                u.setNombre(usuario.getNombre());
    	                u.setTipoUsuario(usuario.getTipoUsuario());
    	                u.setRol(usuario.getRol());
    	                u.setEmpresa(usuario.getEmpresa());
    	                
    	                // IMPORTANTE: nos aseguramos de que esté activo al actualizar
    	                u.setActivo(true); 
    	                
    	                return usuarioRepository.save(u); // Ejecuta un UPDATE
    	            })
    	            .orElseGet(() -> {
    	                // Si NO existe: guardamos el nuevo objeto directamente
    	                return usuarioRepository.save(usuario); // Ejecuta un INSERT
    	            });
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
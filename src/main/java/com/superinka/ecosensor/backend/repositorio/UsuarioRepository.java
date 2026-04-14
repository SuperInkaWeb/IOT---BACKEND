package com.superinka.ecosensor.backend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.superinka.ecosensor.backend.modelo.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	 @Query("SELECT u FROM Usuario u " +
	           " LEFT JOIN FETCH u.empresa e " +
	           " LEFT JOIN FETCH e.plan p " +
	           "WHERE u.email = :email")
	    Optional<Usuario> findByEmailWithPlan(@Param("email") String email);

	    List<Usuario> findByEmpresaId(Long empresaId);
    
}
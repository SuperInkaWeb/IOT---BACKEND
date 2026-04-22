package com.superinka.ecosensor.backend.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Relación con Empresa
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id", nullable = true)
    @JsonIgnoreProperties({"usuarios", "sensores", "hibernateLazyInitializer", "handler"})
    private Empresa empresa;
    
    @Column(name = "recibir_alertas_email")
    private Boolean recibirAlertasEmail = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario")
    private TipoUsuario tipoUsuario;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", columnDefinition = "TEXT")
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = true) // <--- ESTO ES VITAL
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Plan plan;
    
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.VISOR;	

    private Boolean activo = true;

    private LocalDateTime fechaCreacion;
}
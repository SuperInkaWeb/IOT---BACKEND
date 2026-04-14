package com.superinka.ecosensor.backend.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;	

    @Column(unique = true, length = 20)
    private String ruc;

    @Column(length = 150)
    private String emailContacto;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;
    
    @Column
    private Boolean activa = true;

    private LocalDateTime fechaCreacion;
    
    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Plan plan;

    @OneToMany(mappedBy = "empresa")
    @JsonIgnore
    private List<Usuario> usuarios;
    
    // 🔹 Relación inversa con Sensor
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Sensor> sensores;
    
    @OneToMany(mappedBy = "empresa", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"empresa", "hibernateLazyInitializer"})
    private List<Suscripcion> suscripciones = new ArrayList<>();
}
package com.superinka.ecosensor.backend.modelo;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "sensor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Relación con empresa
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    @JsonIgnoreProperties({"sensores", "usuarios", "plan", "hibernateLazyInitializer", "handler"})
    private Empresa empresa;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnoreProperties({"sensores", "empresa"})
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 100)
    private String deviceId;
    
    @Column(nullable = false, length = 100)
    private String tipo;

    @Column(length = 100)
    private String modelo;

    @Column(length = 150)
    private String ubicacion;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDate fechaInstalacion;
    
    @Column(nullable = false)
    private Boolean esGlobal = false;

    @Column(precision = 3, scale = 2)
    private BigDecimal alturaInstalacion;
    
}
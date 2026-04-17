package com.superinka.ecosensor.backend.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "suscripcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false) 
    private Usuario usuario;
    
    // 🔹 Relación con Empresa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = true)
    private Empresa empresa;

    // 🔹 Relación con Plan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;
    
    @Column(length = 100)
    private String paymeOrderId;      // ID de orden de Pay-me

    @Column(length = 20)
    private String estadoPago;         // PENDIENTE, PAGADO, RECHAZADO

    @Column(length = 20)
    private String moneda = "PEN";

    @Column(length = 20)
    private String estado = "ACTIVA"; // ACTIVA, VENCIDA, CANCELADA
}
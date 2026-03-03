package com.superinka.ecosensor.backend.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "precio_mensual", precision = 10, scale = 2)
    private BigDecimal precioMensual;  // NUMERIC(10,2) → BigDecimal

    private Integer limiteSensores;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
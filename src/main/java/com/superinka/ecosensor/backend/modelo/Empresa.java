package com.superinka.ecosensor.backend.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empresa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    private LocalDateTime fechaCreacion;

    // 🔹 Relación inversa con Sensor
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    private List<Sensor> sensores;
}
package com.superinka.ecosensor.backend.dto;

import lombok.Data;

@Data
public class EmpresaRequest {

    private String nombre;
    private String ruc;
    private String emailContacto;
    private Long planId;
    private Long creadorId;

}
package com.superinka.ecosensor.backend.dto;

import com.superinka.ecosensor.backend.modelo.Usuario;
import com.superinka.ecosensor.backend.modelo.Suscripcion;

public class UsuarioResponseDTO {

    private Long   id;
    private String nombre;
    private String email;
    private String rol;
    private Long   empresaId;
    private String empresaNombre;
    private String tipoUsuario;
    private String planNombre;     // 🔥 nuevo — para el dashboard frontend

    public UsuarioResponseDTO(Usuario u) {
        this.id          = u.getId();
        this.nombre      = u.getNombre();
        this.email       = u.getEmail();
        this.rol         = u.getRol()         != null ? u.getRol().name()         : null;
        this.tipoUsuario = u.getTipoUsuario() != null ? u.getTipoUsuario().name() : null;

        if (u.getEmpresa() != null) {
            this.empresaId     = u.getEmpresa().getId();
            this.empresaNombre = u.getEmpresa().getNombre();

            // 🔥 buscar suscripción activa de la empresa para obtener el plan
            if (u.getEmpresa().getSuscripciones() != null) {
                u.getEmpresa().getSuscripciones().stream()
                    .filter(s -> "ACTIVA".equals(s.getEstado()))
                    .findFirst()
                    .ifPresentOrElse(
                        s -> this.planNombre = s.getPlan() != null ? s.getPlan().getNombre() : "Básico",
                        () -> this.planNombre = "Básico"
                    );
            } else {
                this.planNombre = "Básico";
            }
        } else {
            this.planNombre = "Básico";
        }
    }

    public Long   getId()           { return id; }
    public String getNombre()       { return nombre; }
    public String getEmail()        { return email; }
    public String getRol()          { return rol; }
    public Long   getEmpresaId()    { return empresaId; }
    public String getEmpresaNombre(){ return empresaNombre; }
    public String getTipoUsuario()  { return tipoUsuario; }
    public String getPlanNombre()   { return planNombre; }
}
package com.superinka.ecosensor.backend.dto;

import com.superinka.ecosensor.backend.modelo.Rol;
import com.superinka.ecosensor.backend.modelo.TipoUsuario;

public class CompletarPerfilDTO {

	private String nombre;
	private String email;
    private Long empresaId; // ✅ Long
    private TipoUsuario tipoUsuario;
    private boolean recibirAlertasEmail;
    
    private String empresaNombre;
    private String ruc;
    private Long   planId;
    
    public CompletarPerfilDTO() {}
    
    // getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    
    public boolean isRecibirAlertasEmail() { 
        return recibirAlertasEmail; 
    }

    public void setRecibirAlertasEmail(boolean recibirAlertasEmail) { 
        this.recibirAlertasEmail = recibirAlertasEmail; 
    }
    
    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    
}

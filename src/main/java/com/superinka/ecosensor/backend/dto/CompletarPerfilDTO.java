package com.superinka.ecosensor.backend.dto;

import com.superinka.ecosensor.backend.modelo.Rol;
import com.superinka.ecosensor.backend.modelo.TipoUsuario;

public class CompletarPerfilDTO {

	private String nombre;
    private Rol rol;
    private Long empresaId; // ✅ Long
    private TipoUsuario tipoUsuario;
    private boolean recibirAlertasEmail;
    
    public CompletarPerfilDTO() {}
    
    // getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    
    public boolean isRecibirAlertasEmail() { 
        return recibirAlertasEmail; 
    }

    public void setRecibirAlertasEmail(boolean recibirAlertasEmail) { 
        this.recibirAlertasEmail = recibirAlertasEmail; 
    }
}

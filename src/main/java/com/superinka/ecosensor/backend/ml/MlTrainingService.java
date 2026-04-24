package com.superinka.ecosensor.backend.ml;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.modelo.Empresa;
import com.superinka.ecosensor.backend.modelo.LecturaSensor;
import com.superinka.ecosensor.backend.modelo.TipoMetrica;
import com.superinka.ecosensor.backend.repositorio.EmpresaRepository;
import com.superinka.ecosensor.backend.repositorio.LecturaSensorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MlTrainingService {

    private final LecturaSensorRepository lecturaRepository;
    private final AnomalyDetectionService anomalyService;
    private final EmpresaRepository empresaRepository;

    public void entrenarModelo(Long ownerId, String tipo) {
        try {
            TipoMetrica tipoEnum = TipoMetrica.valueOf(tipo.toUpperCase());
            List<LecturaSensor> lecturas = lecturaRepository.obtenerLecturasHistoricas(ownerId, tipoEnum);

            // Mínimo 50 para que el Isolation Forest sea estadísticamente serio
            if (lecturas == null || lecturas.size() < 50) return;

            double[][] data = new double[lecturas.size()][3];
            for (int i = 0; i < lecturas.size(); i++) {
                LecturaSensor l = lecturas.get(i);
                data[i][0] = l.getValor() != null ? l.getValor() : 0;
                data[i][1] = l.getTemperatura() != null ? l.getTemperatura() : 0;
                data[i][2] = l.getHumedad() != null ? l.getHumedad() : 0;
            }

            // Usamos el mismo formato de KEY que usa LecturaSensorServiceImpl
            String key = ownerId + "_" + tipoEnum.name();
            anomalyService.entrenarModelo(key, data);
            
        } catch (Exception e) {
            System.err.println("Error entrenando métrica " + tipo + " para owner " + ownerId + ": " + e.getMessage());
        }
    }
    
    public void entrenarModeloGlobal() {

    	List<Empresa> empresas = empresaRepository.findAll(); 

        String[] metricas = {
                "pm25",
                "co2",
                "temperatura",
                "energia"
        };

        for (Empresa emp : empresas) {
            for (String tipo : metricas) {
                entrenarModelo(emp.getId(), tipo);
            }
        }
    }
}
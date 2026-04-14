package com.superinka.ecosensor.backend.ml;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.superinka.ecosensor.backend.modelo.LecturaSensor;
import com.superinka.ecosensor.backend.modelo.TipoMetrica;
import com.superinka.ecosensor.backend.repositorio.LecturaSensorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MlTrainingService {

    private final LecturaSensorRepository lecturaRepository;
    private final AnomalyDetectionService anomalyService;

    public void entrenarModelo(Long empresaId, String tipo) {
    	
        TipoMetrica tipoEnum = TipoMetrica.valueOf(tipo.toUpperCase());


        List<LecturaSensor> lecturas =
                lecturaRepository.obtenerLecturasHistoricas(empresaId, tipoEnum);
        
        
        if (lecturas.size() < 20) {
            return;
        }
        
        
        double[][] data = new double[lecturas.size()][3];

        for (int i = 0; i < lecturas.size(); i++) {
        	
        	LecturaSensor l = lecturas.get(i);

            double v = l.getValor() != null ? l.getValor() : 0;
            double t = l.getTemperatura() != null ? l.getTemperatura() : 0;
            double h = l.getHumedad() != null ? l.getHumedad() : 0;

            data[i][0] = v;     // valor
            data[i][1] = t;    // temperatura simulada (temporal)
            data[i][2] = h;    // humedad simulada
        } 
        
        String key = empresaId + "_" + tipo;

        anomalyService.entrenarModelo(key, data);
    }
    
    public void entrenarModeloGlobal() {

        Long empresaId = 1L; // temporal mientras pruebas

        String[] metricas = {
                "pm25",
                "co2",
                "temperatura",
                "energia"
        };

        for (String tipo : metricas) {
            entrenarModelo(empresaId, tipo);
        }
    }
}
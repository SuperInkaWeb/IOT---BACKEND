package com.superinka.ecosensor.backend.ml;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import smile.anomaly.IsolationForest;

@Service
public class AnomalyDetectionService {

	private final Map<String, IsolationForest> modelos = new ConcurrentHashMap<>();

    // Entrenar modelo con datos históricos
    public void entrenarModelo(String key, double[][] data) {
    	
    	if (data == null || data.length < 10) 
            return; // muy pocos datos para entrenar
        
    	IsolationForest model = IsolationForest.fit(data);

        modelos.put(key, model);
    }

    // Evaluar si un valor es anomalía
    public boolean esAnomalia(String key, Double valor, Double temperatura, Double humedad) {
    	
    	IsolationForest model = modelos.get(key);

        if (model == null) {
            return false;
        }

        try {
            double[] punto = { 
                valor != null ? valor : 0, 
                temperatura != null ? temperatura : 0, 
                humedad != null ? humedad : 0 
            };
            return model.score(punto) > 0.7;
        } catch (Exception e) {
            // Si el punto no coincide con el entrenamiento, evitamos que se caiga el servidor
            return false;
        }
    }
    
    public boolean modeloEntrenado() {
        return !modelos.isEmpty();
    }
    
}

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
	    // 1. Validación de seguridad: Necesitas un mínimo de datos para que el bosque sea estable
	    if (data == null || data.length < 20) { 
	        return; 
	    }

	    try {
	        // 2. Volvemos a la firma estándar que no da error
	        // La mayoría de las versiones de Smile usan esta:
	        IsolationForest model = IsolationForest.fit(data); 

	        modelos.put(key, model);
	    } catch (Exception e) {
	        // Evitamos que un error de entrenamiento tire abajo todo el backend
	        System.err.println("Error entrenando modelo ML para " + key + ": " + e.getMessage());
	    }
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

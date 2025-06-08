package com.proyecto.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.backend.ml.MLService;

@RestController
public class MLController {

    private final MLService mlService;

    public MLController(MLService mlService) {
        this.mlService = mlService;
    }

    @PostMapping("/api/predict")
    public ResponseEntity<?> predecirAnimal(@RequestParam("image") MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Por favor, selecciona un archivo de imagen para subir.");
        }
        try {
            Map<String, Object> predictionResult = mlService.getPrediction(imageFile);
            return ResponseEntity.ok(predictionResult);
        } catch (Exception e) {
            // Devolvemos un error 500 (Internal Server Error) si algo sale mal
            return ResponseEntity.status(500).body("Error en el servidor al procesar la predicción: " + e.getMessage());
        }
    }
}
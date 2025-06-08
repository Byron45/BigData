package com.proyecto.backend.ml;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MLService {

    private static final Logger logger = LoggerFactory.getLogger(MLService.class);
    private final RestTemplate restTemplate;

    // Inyectamos la URL desde application.properties
    @Value("${ml.service.url}")
    private String mlServiceUrl;

    public MLService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("rawtypes") // Para suprimir advertencia en ResponseEntity<Map>
    public Map<String, Object> getPrediction(MultipartFile imageFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // ¡IMPORTANTE! Debemos envolver los bytes del archivo en un ByteArrayResource
            // para que RestTemplate pueda manejarlo correctamente como un archivo.
            ByteArrayResource resource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String predictUrl = mlServiceUrl + "/predict";
            logger.info("Enviando petición de predicción a: {}", predictUrl);

            ResponseEntity<Map> response = restTemplate.postForEntity(predictUrl, requestEntity, Map.class);
            
            logger.info("Predicción recibida exitosamente.");
            return response.getBody();

        } catch (Exception e) {
            logger.error("Error al llamar al servicio de ML", e);
            // Aquí podrías lanzar una excepción personalizada para un mejor manejo de errores.
            throw new RuntimeException("Error al comunicarse con el servicio de predicción: " + e.getMessage());
        }
    }
}
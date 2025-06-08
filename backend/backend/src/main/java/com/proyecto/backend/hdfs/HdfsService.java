package com.proyecto.backend.hdfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HdfsService {

    private static final Logger logger = LoggerFactory.getLogger(HdfsService.class);

    @Value("${spring.hdfs.uri:hdfs://namenode:8020}")
    private String hdfsUri;

    private List<Map<String, Object>> cachedParkData;

    private Configuration getHadoopConfiguration() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsUri);
        return conf;
    }

    @PostConstruct
    public void inicializarCache() {
        logger.info("==============================================");
        logger.info("INICIANDO CACHÉ: Cargando datos de Parques desde HDFS...");
        this.cachedParkData = leerParquesDesdeHdfsInterno();
        logger.info("CACHÉ INICIALIZADO: {} parques cargados en memoria.", (cachedParkData != null ? cachedParkData.size() : 0));
        logger.info("==============================================");
    }
    
    public List<Map<String, Object>> obtenerParquesDesdeCache() {
        logger.info("Sirviendo {} parques desde el caché.", (cachedParkData != null ? cachedParkData.size() : 0));
        return this.cachedParkData != null ? this.cachedParkData : Collections.emptyList();
    }

    private List<Map<String, Object>> leerParquesDesdeHdfsInterno() {
        String rutaArchivo = "/user/hadoop/datasets/park-biodiversity/parks.csv";
        List<Map<String, Object>> parques = new ArrayList<>();
        logger.info("Lectura desde HDFS: {}", rutaArchivo);

        try (FileSystem fs = FileSystem.get(getHadoopConfiguration())) {
            Path filePath = new Path(rutaArchivo);
            if (!fs.exists(filePath)) {
                logger.error("El archivo de parques no existe en HDFS: {}", rutaArchivo);
                return parques;
            }

            try (FSDataInputStream inputStream = fs.open(filePath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                
                String headerLine = reader.readLine();
                if (headerLine == null) return parques;
                
                String[] headers = headerLine.split(",");
                
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    Map<String, Object> parque = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        if (i < values.length) {
                           String cleanedValue = values[i].trim().replaceAll("^\"|\"$", "");
                           parque.put(headers[i].trim(), cleanedValue);
                        }
                    }
                    if (!parque.isEmpty()) {
                        parques.add(parque);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error leyendo archivo de parques desde HDFS", e);
        }
        return parques;
    }
}
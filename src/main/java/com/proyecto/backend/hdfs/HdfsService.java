package com.proyecto.backend.hdfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HdfsService {

    private static final Logger logger = LoggerFactory.getLogger(HdfsService.class);

    private Configuration getHadoopConfiguration() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://namenode:8020");
        conf.set("hadoop.security.authentication", "simple");
        UserGroupInformation.setConfiguration(conf);
        return conf;
    }

    public void listarArchivos(String ruta) {
        try {
            Configuration conf = getHadoopConfiguration();
            FileSystem fs = FileSystem.get(conf);
            FileStatus[] fileStatuses = fs.listStatus(new Path(ruta));
            for (FileStatus status : fileStatuses) {
                logger.info("Archivo/carpeta: " + status.getPath().toString());
            }
            fs.close();
        } catch (Exception e) {
            logger.error("Error listando archivos en HDFS", e);
        }
    }

    // Recursivo: lista todos los archivos en la ruta y subcarpetas
    public List<String> obtenerArchivos(String ruta) {
        List<String> archivos = new ArrayList<>();
        try {
            Configuration conf = getHadoopConfiguration();
            FileSystem fs = FileSystem.get(conf);
            listarArchivosRecursivo(fs, new Path(ruta), archivos);
            fs.close();
        } catch (Exception e) {
            logger.error("Error obteniendo archivos de HDFS", e);
        }
        return archivos;
    }

    private void listarArchivosRecursivo(FileSystem fs, Path path, List<String> archivos) throws Exception {
        logger.info("Revisando: " + path.toString());
        FileStatus[] fileStatuses = fs.listStatus(path);
        for (FileStatus status : fileStatuses) {
            if (status.isFile()) {
                logger.info("Archivo encontrado: " + status.getPath().toString());
                archivos.add(status.getPath().toString());
            } else if (status.isDirectory()) {
                listarArchivosRecursivo(fs, status.getPath(), archivos);
            }
        }
    }

    // Recursivo: lee todos los CSV en la ruta y subcarpetas
    public List<Map<String, Object>> leerCsvDesdeHdfs(String ruta) {
        List<Map<String, Object>> resultados = new ArrayList<>();
        try {
            Configuration conf = getHadoopConfiguration();
            FileSystem fs = FileSystem.get(conf);
            leerCsvRecursivo(fs, new Path(ruta), resultados);
            fs.close();
        } catch (Exception e) {
            logger.error("Error leyendo CSVs de HDFS", e);
        }
        return resultados;
    }

    private void leerCsvRecursivo(FileSystem fs, Path path, List<Map<String, Object>> resultados) throws Exception {
        logger.info("Buscando CSVs en: " + path.toString());
        FileStatus[] fileStatuses = fs.listStatus(path);
        for (FileStatus status : fileStatuses) {
            if (status.isFile() && status.getPath().getName().endsWith(".csv")) {
                logger.info("Leyendo CSV: " + status.getPath().toString());
                try (FSDataInputStream inputStream = fs.open(status.getPath());
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String headerLine = reader.readLine();
                    if (headerLine == null) continue;
                    String[] headers = headerLine.split(",");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 0; i < headers.length && i < values.length; i++) {
                            row.put(headers[i], values[i]);
                        }
                        resultados.add(row);
                    }
                }
            } else if (status.isDirectory()) {
                leerCsvRecursivo(fs, status.getPath(), resultados);
            }
        }
    }
}
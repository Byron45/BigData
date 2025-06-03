package com.proyecto.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.backend.hdfs.HdfsService;

@RestController
public class TestHdfsController {

    private final HdfsService hdfsService;

    public TestHdfsController(HdfsService hdfsService) {
        this.hdfsService = hdfsService;
    }

    @GetMapping("/test-hdfs")
    public String testHdfs() {
        hdfsService.listarArchivos("/user/hadoop/datasets");
        return "Revisar logs para ver archivos listados";
    }

    @GetMapping("/api/archivos")
    public List<String> obtenerArchivos() {
        return hdfsService.obtenerArchivos("/user/hadoop/datasets");
    }

    @GetMapping("/api/resultados/incendios")
    public List<Map<String, Object>> obtenerResultadosIncendios() {
        return hdfsService.leerCsvDesdeHdfs("/user/hadoop/results/forestfires_by_month");
    }
}
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
    
    @GetMapping("/api/parques")
    public List<Map<String, Object>> obtenerParques() {
        return hdfsService.obtenerParquesDesdeCache();
    }
}
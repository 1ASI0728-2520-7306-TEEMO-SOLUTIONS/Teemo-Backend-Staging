package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.infrastructure.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

// src/main/java/.../ai/infrastructure/rest/ModelInfoController.java
@RestController
@RequestMapping("/api/ai")
public class ModelInfoController {

    @Value("${ai.model.name}")    private String name;
    @Value("${ai.model.version}") private String version;
    @Value("${ai.model.onnx}")    private String onnxPath;

    @GetMapping("/model-info")
    public Map<String, Object> info() {g
        return Map.of(
                "name", name,
                "version", version,
                "onnxPath", onnxPath
        );
    }
}

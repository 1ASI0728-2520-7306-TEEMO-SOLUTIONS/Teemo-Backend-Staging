package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.config;


import ai.onnxruntime.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class ModelConfig {

    @Value("${AI_MODEL_PATH:classpath:model/weather-delay.onnx}")
    private String modelPath;

    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }

    @Bean
    public OrtSession ortSession(OrtEnvironment env) throws Exception {
        if (modelPath.startsWith("classpath:")) {
            String cp = modelPath.replace("classpath:", "").replaceFirst("^/", ""); // quita prefijo y slashes sobrantes
            var resource = new ClassPathResource(cp);
            if (!resource.exists()) {
                throw new IllegalStateException("ONNX no encontrado en classpath:" + cp);
            }
            try (var is = resource.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                return env.createSession(bytes, new OrtSession.SessionOptions());
            }
        } else {
            return env.createSession(modelPath, new OrtSession.SessionOptions());
        }
    }
}


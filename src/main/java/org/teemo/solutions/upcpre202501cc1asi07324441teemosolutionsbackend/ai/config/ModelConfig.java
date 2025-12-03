package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class ModelConfig {

    private static final Logger log = LoggerFactory.getLogger(ModelConfig.class);

    @Value("${AI_MODEL_PATH:classpath:model/weather-delay.onnx}")
    private Resource modelResource;

    @Bean
    public OrtEnvironment ortEnvironment() {
        return OrtEnvironment.getEnvironment();
    }

    @Bean
    public OrtSession ortSession(OrtEnvironment env) throws OrtException, IOException {
        if (!modelResource.exists()) {
            throw new IllegalStateException("ONNX model not found: " + modelResource);
        }

        try (var is = modelResource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            log.info("Loading ONNX model from {} ({} bytes)", modelResource, bytes.length);

            var options = new OrtSession.SessionOptions();
            return env.createSession(bytes, options);
        }
    }
}

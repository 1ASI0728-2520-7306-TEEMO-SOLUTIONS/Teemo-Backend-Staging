package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Loads preprocessing metadata (feature order, scaler params and label encoders) generated with the ONNX model
 * and exposes a helper to turn raw feature maps into the standardized float vector expected by the session.
 */
@Component
public class WeatherModelPreprocessor {

    private static final Logger log = LoggerFactory.getLogger(WeatherModelPreprocessor.class);

    private final List<String> featureOrder;
    private final double[] mean;
    private final double[] scale;
    private final Map<String, List<String>> encoderClasses;

    public WeatherModelPreprocessor(
            ObjectMapper mapper,
            @Value("${AI_PREPROCESS_PATH:classpath:model/preprocess.json}") String preprocessPath
    ) throws IOException {
        Map<String, Object> root = readJson(mapper, preprocessPath);
        this.featureOrder = Collections.unmodifiableList(readFeatureOrder(root));
        this.mean = readNumberArray(root, "scaler_mean_", "scaler_mean");
        this.scale = readNumberArray(root, "scaler_scale_", "scaler_scale");
        this.encoderClasses = Collections.unmodifiableMap(readEncoders(root));

        if (featureOrder.size() != mean.length || mean.length != scale.length) {
            throw new IllegalStateException("preprocess.json inconsistent lengths: features="
                    + featureOrder.size() + " mean=" + mean.length + " scale=" + scale.length);
        }

        log.info("WeatherModelPreprocessor ready with {} features ({} categorical)",
                featureOrder.size(), encoderClasses.size());
    }

    public float[] prepareInput(Map<String, Double> numericValues, Map<String, String> categoricalValues) {
        float[] standardized = new float[featureOrder.size()];
        for (int i = 0; i < featureOrder.size(); i++) {
            String feature = featureOrder.get(i);
            double rawValue;
            if (encoderClasses.containsKey(feature)) {
                rawValue = encodeCategorical(feature, categoricalValues.get(feature))
                        .orElse(mean[i]);
            } else {
                rawValue = numericValues.getOrDefault(feature, mean[i]);
            }
            double scaled = scale[i] == 0 ? (rawValue - mean[i]) : (rawValue - mean[i]) / scale[i];
            standardized[i] = (float) scaled;
        }
        return standardized;
    }

    private Map<String, Object> readJson(ObjectMapper mapper, String location) throws IOException {
        Resource resource;
        if (location.startsWith("classpath:")) {
            String cp = location.replace("classpath:", "").replaceFirst("^/", "");
            resource = new ClassPathResource(cp);
            if (!resource.exists()) {
                throw new IllegalStateException("preprocess.json not found at classpath:" + cp);
            }
        } else {
            resource = new FileSystemResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("preprocess.json not found at " + location);
            }
        }

        try (InputStream is = resource.getInputStream()) {
            return mapper.readValue(is, new TypeReference<>() {});
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readFeatureOrder(Map<String, Object> root) {
        Object raw = root.getOrDefault("features_order", root.get("feature_order"));
        if (!(raw instanceof List<?>)) {
            throw new IllegalStateException("preprocess.json missing features_order array");
        }
        List<?> list = (List<?>) raw;
        if (list.isEmpty()) {
            throw new IllegalStateException("preprocess.json missing features_order array");
        }
        List<String> out = new ArrayList<>(list.size());
        for (Object item : list) {
            out.add(Objects.toString(item));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private double[] readNumberArray(Map<String, Object> root, String preferredKey, String altKey) {
        Object raw = root.getOrDefault(preferredKey, root.get(altKey));
        if (!(raw instanceof List<?>)) {
            throw new IllegalStateException("preprocess.json missing array for key " + preferredKey);
        }
        List<?> list = (List<?>) raw;
        if (list.isEmpty()) {
            throw new IllegalStateException("preprocess.json missing array for key " + preferredKey);
        }
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object val = list.get(i);
            if (!(val instanceof Number)) {
                throw new IllegalStateException("Expected number at index " + i + " for key " + preferredKey);
            }
            arr[i] = ((Number) val).doubleValue();
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> readEncoders(Map<String, Object> root) {
        Object raw = root.get("encoders");
        if (!(raw instanceof Map<?, ?>)) {
            return Collections.emptyMap();
        }
        Map<?, ?> map = (Map<?, ?>) raw;
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object detailRaw = entry.getValue();
            if (!(detailRaw instanceof Map<?, ?>)) {
                continue;
            }
            Map<?, ?> detail = (Map<?, ?>) detailRaw;
            Object classesRaw = detail.containsKey("classes_") ? detail.get("classes_") : detail.get("classes");
            if (!(classesRaw instanceof List<?>)) {
                continue;
            }
            List<?> list = (List<?>) classesRaw;
            if (list.isEmpty()) {
                continue;
            }
            List<String> classes = new ArrayList<>(list.size());
            for (Object item : list) {
                classes.add(Objects.toString(item));
            }
            out.put(String.valueOf(entry.getKey()), classes);
        }
        return out;
    }

    private Optional<Double> encodeCategorical(String feature, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        List<String> classes = encoderClasses.get(feature);
        if (classes == null || classes.isEmpty()) {
            return Optional.empty();
        }
        String normalizedValue = normalize(rawValue);
        for (int i = 0; i < classes.size(); i++) {
            String candidate = classes.get(i);
            if (candidate.equalsIgnoreCase(rawValue) || normalize(candidate).equals(normalizedValue)) {
                return Optional.of((double) i);
            }
        }
        log.debug("Value '{}' not found in encoder '{}', using mean", rawValue, feature);
        return Optional.empty();
    }

    private String normalize(String input) {
        String trimmed = input.trim();
        String decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}

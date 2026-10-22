package com.essenza.aws.modules.amazonSNS.infrastructure.outbound.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonMapConverter {

    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public String toJson(Map<String, String> map) {
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error serializing map to JSON", e);
        }
    }

    public Map<String, String> toMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parsing JSON payload", e);
        }
    }
}

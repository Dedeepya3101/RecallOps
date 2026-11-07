package io.recallops.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@RestController
public class HealthController {
    private final boolean openAiConfigured;
    public HealthController(@Value("${recallops.openai.api-key:}") String apiKey) { this.openAiConfigured = apiKey != null && !apiKey.isBlank(); }
    @GetMapping("/api/health") public Map<String,String> health(){return Map.of("status","UP","service","recallops-api");}
    @GetMapping("/api/config") public Map<String,Object> config(){return Map.of("openAiConfigured",openAiConfigured);}
}

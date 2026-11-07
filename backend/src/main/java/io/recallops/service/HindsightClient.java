package io.recallops.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HindsightClient {
    private final RestClient client;
    private final String apiKey, bankId;
    private final ObjectMapper mapper;
    public HindsightClient(@Value("${recallops.hindsight.url}") String url,
            @Value("${recallops.hindsight.api-key}") String apiKey,
            @Value("${recallops.hindsight.bank-id}") String bankId, ObjectMapper mapper) {
        JdkClientHttpRequestFactory factory=new JdkClientHttpRequestFactory(); factory.setReadTimeout(Duration.ofSeconds(20));
        this.client=RestClient.builder().baseUrl(url).requestFactory(factory).build(); this.apiKey=apiKey; this.bankId=bankId; this.mapper=mapper;
    }
    public Recall recall(String query) {
        if (apiKey.isBlank()) return new Recall(false, List.of());
        try {
            JsonNode body=client.post().uri("/v1/default/banks/{bank}/memories/recall",bankId)
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+apiKey).body(Map.of("query",query,"budget","low","max_tokens",1200))
                .retrieve().body(JsonNode.class);
            List<String> items=new ArrayList<>(); collect(body,items);
            return new Recall(true,items.stream().limit(6).toList());
        } catch (RestClientException ex) { return new Recall(false,List.of()); }
    }
    private void collect(JsonNode node,List<String> into) {
        if(node==null)return;
        if(node.isArray()){node.forEach(n->collect(n,into));return;}
        if(node.isObject()){
            for(String k:List.of("results","memories","items","facts")) if(node.has(k)){collect(node.get(k),into);return;}
            String value=firstText(node,"text","content","fact","memory");
            if(value!=null&&!value.isBlank())into.add(value);
            return;
        }
        if(node.isTextual())into.add(node.asText());
    }
    private String firstText(JsonNode n,String... keys){for(String key:keys)if(n.path(key).isTextual())return n.path(key).asText();return null;}
    public boolean retain(String documentId,String content,Map<String,Object> metadata) {
        if(apiKey.isBlank())return false;
        try {
            client.post().uri("/v1/default/banks/{bank}/memories",bankId)
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+apiKey)
                .body(Map.of("items",List.of(Map.of("content",content,"document_id",documentId,"metadata",metadata)),"async",false))
                .retrieve().toBodilessEntity(); return true;
        } catch(RestClientException ex){return false;}
    }
    public record Recall(boolean available,List<String> items) {}
}

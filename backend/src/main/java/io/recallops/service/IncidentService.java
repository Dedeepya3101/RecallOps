package io.recallops.service;

import io.recallops.api.IncidentDtos.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IncidentService {
    private final HindsightClient hindsight; private final OpenAiDiagnosisService model; private final SecretRedactor redactor;
    private final Map<String,Stored> incidents=new ConcurrentHashMap<>();
    public IncidentService(HindsightClient hindsight,OpenAiDiagnosisService model,SecretRedactor redactor){this.hindsight=hindsight;this.model=model;this.redactor=redactor;}
    public AnalyzeResponse analyze(AnalyzeRequest request){
        AnalyzeRequest safe=new AnalyzeRequest(redactor.redact(request.title()),redactor.redact(request.service()),redactor.redact(request.error()),redactor.redact(request.logs()),redactor.redact(request.code()));
        String query="Service: "+safe.service()+"\nError: "+safe.error()+"\nLogs: "+safe.logs();
        HindsightClient.Recall recalled=hindsight.recall(query);
        OpenAiDiagnosisService.Diagnosis diagnosis=model.diagnose(safe,recalled.items());
        String id=UUID.randomUUID().toString(); Instant now=Instant.now();
        Stored stored=new Stored(id,safe,diagnosis,now,"Investigating",false); incidents.put(id,stored);
        List<Memory> memories=recalled.items().stream().map(text->new Memory(text.length()>72?text.substring(0,72)+"…":text,safe.service(),"Historical","—",text)).toList();
        return new AnalyzeResponse(id,diagnosis.probableRootCause(),diagnosis.confidence(),diagnosis.facts(),diagnosis.hypotheses(),diagnosis.missingEvidence(),diagnosis.steps(),diagnosis.fix(),recalled.available(),memories,now);
    }
    public FeedbackResponse feedback(String id,FeedbackRequest request){
        Stored old=incidents.get(id); if(old==null)throw new NoSuchElementException("Incident not found.");
        if(!request.outcome().equals("successful")&&!request.outcome().equals("unsuccessful"))throw new IllegalArgumentException("Outcome must be successful or unsuccessful.");
        String cause=redactor.redact(Optional.ofNullable(request.rootCause()).filter(s->!s.isBlank()).orElse(old.diagnosis().probableRootCause()));
        String resolution=redactor.redact(Optional.ofNullable(request.resolution()).filter(s->!s.isBlank()).orElse(old.diagnosis().fix()));
        String status=request.outcome().equals("successful")?"Resolved":"Unsuccessful";
        boolean retained=false;
        if(request.outcome().equals("successful")){
            String summary="Verified incident resolution. Service/framework: "+old.request().service()+". Symptoms: "+old.request().error()+". Root cause confirmed by developer: "+cause+". Verified resolution: "+resolution+". Unsuccessful attempts: "+Optional.ofNullable(request.notes()).map(redactor::redact).orElse("Not provided")+". Outcome: successful.";
            retained=hindsight.retain("incident-"+id,summary,Map.of("service",old.request().service(),"outcome","successful","incident_id",id));
        }
        incidents.put(id,new Stored(id,old.request(),old.diagnosis(),old.createdAt(),status,retained));
        return new FeedbackResponse(id,request.outcome(),retained,retained?"Verified resolution retained in Hindsight.":request.outcome().equals("successful")?"Outcome recorded, but Hindsight retention is unavailable or not configured.":"Outcome recorded; unverified guesses were not retained.");
    }
    public List<HistoryItem> history(){return incidents.values().stream().sorted(Comparator.comparing(Stored::createdAt).reversed()).map(s->new HistoryItem(s.id(),s.request().title(),s.request().service(),s.createdAt(),s.status(),s.diagnosis().probableRootCause(),s.retained())).toList();}
    private record Stored(String id,AnalyzeRequest request,OpenAiDiagnosisService.Diagnosis diagnosis,Instant createdAt,String status,boolean retained){}
}

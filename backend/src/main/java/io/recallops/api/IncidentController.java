package io.recallops.api;

import io.recallops.api.IncidentDtos.*;
import io.recallops.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidents;
    public IncidentController(IncidentService incidents){this.incidents=incidents;}
    @PostMapping("/analyze") public AnalyzeResponse analyze(@Valid @RequestBody AnalyzeRequest request){return incidents.analyze(request);}
    @PostMapping("/{id}/feedback") public FeedbackResponse feedback(@PathVariable String id,@Valid @RequestBody FeedbackRequest request){return incidents.feedback(id,request);}
    @GetMapping("/history") public List<HistoryItem> history(){return incidents.history();}
}

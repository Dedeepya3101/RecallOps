package io.recallops.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class IncidentDtos {
    private IncidentDtos() {}
    public record AnalyzeRequest(@Size(max=180) String title, @NotBlank @Size(max=120) String service,
            @NotBlank @Size(max=12000) String error, @Size(max=30000) String logs, @Size(max=20000) String code) {}
    public record Memory(String title, String service, String date, String match, String note) {}
    public record AnalyzeResponse(String id, String probableRootCause, String confidence, List<String> facts,
            List<String> hypotheses, List<String> missingEvidence, List<String> steps, String fix,
            boolean memoryAvailable, List<Memory> memories, Instant createdAt) {}
    public record FeedbackRequest(@NotBlank String outcome, @Size(max=2000) String rootCause,
            @Size(max=4000) String resolution, @Size(max=2000) String notes) {}
    public record FeedbackResponse(String id, String outcome, boolean retained, String message) {}
    public record HistoryItem(String id, String title, String service, Instant createdAt,
            String status, String rootCause, boolean memoryRetained) {}
    public record ErrorResponse(String message, Instant timestamp) {}
}

package io.recallops.service;

import io.recallops.api.IncidentDtos.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncidentServiceTest {
    @Test void verifiedResolutionIsRetainedAndUnsuccessfulFeedbackIsNot() {
        HindsightClient hindsight=mock(HindsightClient.class);
        OpenAiDiagnosisService model=mock(OpenAiDiagnosisService.class);
        SecretRedactor redactor=new SecretRedactor();
        when(hindsight.recall(anyString())).thenReturn(new HindsightClient.Recall(true,List.of("Prior verified JDBC fix")));
        when(model.diagnose(any(),anyList())).thenReturn(new OpenAiDiagnosisService.Diagnosis("Stale pool", "Likely", List.of("Connection refused"),List.of("Pool validation disabled"),List.of("Check DB readiness"),List.of("Inspect pool"),"Enable validation"));
        when(hindsight.retain(anyString(),anyString(),anyMap())).thenReturn(true);
        IncidentService service=new IncidentService(hindsight,model,redactor);
        AnalyzeResponse response=service.analyze(new AnalyzeRequest("DB down","Spring Boot + MySQL","connection refused",null,null));
        assertTrue(response.memoryAvailable()); assertEquals(1,response.memories().size());
        verify(model).diagnose(any(),eq(List.of("Prior verified JDBC fix")));
        service.feedback(response.id(),new FeedbackRequest("unsuccessful",null,null,"Tried restart"));
        verify(hindsight,never()).retain(anyString(),anyString(),anyMap());
        service.feedback(response.id(),new FeedbackRequest("successful","Pool validation disabled","Enabled SELECT 1 and reduced max lifetime","Restart was not needed"));
        verify(hindsight).retain(contains(response.id()),contains("Enabled SELECT 1"),argThat(m->"successful".equals(m.get("outcome"))));
        assertEquals("Resolved",service.history().get(0).status());
    }
    @Test void memoriesArePassedIntoModelAndUnavailableMemoryIsExplicit() {
        HindsightClient hindsight=mock(HindsightClient.class); OpenAiDiagnosisService model=mock(OpenAiDiagnosisService.class);
        when(hindsight.recall(anyString())).thenReturn(new HindsightClient.Recall(false,List.of()));
        when(model.diagnose(any(),anyList())).thenReturn(new OpenAiDiagnosisService.Diagnosis("Unknown","Low",List.of(),List.of(),List.of("Check DB health"),List.of(),"Gather evidence"));
        IncidentService service=new IncidentService(hindsight,model,new SecretRedactor());
        AnalyzeResponse response=service.analyze(new AnalyzeRequest(null,"Spring Boot","connection failed",null,null));
        assertFalse(response.memoryAvailable()); assertTrue(response.memories().isEmpty());
        verify(model).diagnose(any(),eq(List.of()));
    }
    @Test void secretRedactorRemovesCommonCredentials(){
        String actual=new SecretRedactor().redact("password=supersecret Authorization: Bearer abc.def mysql://root:secret@db");
        assertFalse(actual.contains("supersecret")); assertFalse(actual.contains("abc.def")); assertFalse(actual.contains(":secret@"));
    }
}

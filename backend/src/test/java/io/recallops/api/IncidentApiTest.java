package io.recallops.api;

import io.recallops.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentApiTest {
    @Autowired MockMvc mvc;
    @MockBean IncidentService incidents;

    @Test void healthEndpointIsAvailable() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
    @Test void configEndpointReportsPresenceWithoutReturningSecret() throws Exception {
        mvc.perform(get("/api/config")).andExpect(status().isOk()).andExpect(jsonPath("$.openAiConfigured").isBoolean())
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("sk-"))));
    }
    @Test void analyzeRejectsMissingRequiredFieldsBeforeServiceCall() throws Exception {
        mvc.perform(post("/api/incidents/analyze").contentType(MediaType.APPLICATION_JSON).content("{\"service\":\"Spring Boot\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(incidents);
    }
}

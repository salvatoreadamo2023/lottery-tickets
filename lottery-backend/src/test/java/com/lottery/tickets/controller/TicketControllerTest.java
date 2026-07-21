package com.lottery.tickets.controller;

import com.lottery.tickets.dto.TicketResponse;
import com.lottery.tickets.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "app.security.admin-username=testadmin",
    "app.security.admin-password=testpassword"
})
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    // test validazione: se la data di estrazione è nel passato deve rispondere 400
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createTicket_shouldReturnBadRequest_whenExtractAtIsInThePast() throws Exception {
        String bodyWithPastDate = "{\"extractAt\": \"2020-01-01T00:00:00\"}";

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithPastDate))
                .andExpect(status().isBadRequest());
    }

    // test creazione ticket: se la data è valida (futura) deve rispondere 201
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createTicket_shouldReturnCreated_whenExtractAtIsValid() throws Exception {
        TicketResponse response = new TicketResponse();
        when(ticketService.createTicket(any())).thenReturn(response);

        String validBody = "{\"extractAt\": \"" + LocalDateTime.now().plusDays(30) + "\"}";

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isCreated());
    }

    // test sicurezza: senza autenticazione l'endpoint deve rispondere 401
    @Test
    void createTicket_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        String validBody = "{\"extractAt\": \"" + LocalDateTime.now().plusDays(30) + "\"}";

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isUnauthorized());
    }

    // test ricerca ticket con filtro date: controlla che i parametri from/to vengano accettati e risponda 200
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllTickets_shouldReturnOk_whenFilteringByDateRange() throws Exception {
        when(ticketService.getAllTickets( any(), any(), any()))
                .thenReturn(List.of(new TicketResponse()));

        mockMvc.perform(get("/api/tickets")
                        .param("from", "2024-01-01T00:00:00")
                        .param("to", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
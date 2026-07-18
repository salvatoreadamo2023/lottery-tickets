package com.lottery.tickets.controller;

import com.lottery.tickets.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createTicket_shouldReturnBadRequest_whenExtractAtIsInThePast() throws Exception {
        String bodyWithPastDate = "{\"extractAt\": \"2020-01-01T00:00:00\"}";

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithPastDate))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        String validBody = "{\"extractAt\": \"" + LocalDateTime.now().plusDays(30) + "\"}";

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isUnauthorized());
    }
}
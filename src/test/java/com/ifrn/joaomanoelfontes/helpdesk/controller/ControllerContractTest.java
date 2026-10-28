package com.ifrn.joaomanoelfontes.helpdesk.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.GlobalExceptionHandler;
import com.ifrn.joaomanoelfontes.helpdesk.service.SupportSectorService;
import com.ifrn.joaomanoelfontes.helpdesk.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ControllerContractTest {

    private TicketService ticketService;
    private SupportSectorService sectorService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        sectorService = mock(SupportSectorService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new TicketController(ticketService), new SupportSectorController(sectorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnCreatedForSectorAndTicketCreation() throws Exception {
        mockMvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Tecnologia\",\"sigla\":\"COTIC\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Wi-Fi","descricao":"Sem conexão","local":"Lab 1",
                                 "solicitanteId":1,"categoriaId":1}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnNoContentForSectorDeletion() throws Exception {
        mockMvc.perform(delete("/setores/10")).andExpect(status().isNoContent());
    }

    @Test
    void shouldExposeAllTicketTransitionRoutes() throws Exception {
        mockMvc.perform(patch("/chamados/1/assumir").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":3}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/chamados/1/resolver").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":3,\"solucao\":\"Reiniciado\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/chamados/1/cancelar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"solicitanteId\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/chamados/1/interacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":1,\"texto\":\"Detalhes\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnStandardBadRequestForInvalidBody() throws Exception {
        mockMvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"descricao\":\"\",\"local\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/chamados"));
    }

    @Test
    void shouldReturnStandardConflictForBusinessRule() throws Exception {
        doThrow(new BusinessRuleException("Técnico não pertence ao setor da categoria"))
                .when(ticketService).assign(anyLong(), anyLong());

        mockMvc.perform(patch("/chamados/1/assumir").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":4}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Técnico não pertence ao setor da categoria"))
                .andExpect(jsonPath("$.path").value("/chamados/1/assumir"));
    }
}

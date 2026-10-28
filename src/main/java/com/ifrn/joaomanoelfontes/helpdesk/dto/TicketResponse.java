package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ifrn.joaomanoelfontes.helpdesk.model.Ticket;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.Priority;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.TicketStatus;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        @JsonProperty("titulo") String title,
        @JsonProperty("descricao") String description,
        @JsonProperty("local") String location,
        @JsonProperty("prioridade") Priority priority,
        @JsonProperty("status") TicketStatus status,
        @JsonProperty("dataAbertura") LocalDateTime openedAt,
        @JsonProperty("dataFechamento") @JsonInclude(JsonInclude.Include.NON_NULL) LocalDateTime closedAt,
        @JsonProperty("solucao") @JsonInclude(JsonInclude.Include.NON_NULL) String solution,
        @JsonProperty("solicitante") UserResponse requester,
        @JsonProperty("categoria") CategoryResponse category,
        @JsonProperty("tecnico") @JsonInclude(JsonInclude.Include.NON_NULL) UserResponse assignedTechnician) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getTitle(), ticket.getDescription(), ticket.getLocation(),
                ticket.getPriority(), ticket.getStatus(), ticket.getOpenedAt(), ticket.getClosedAt(),
                ticket.getSolution(), UserResponse.from(ticket.getRequester()), CategoryResponse.from(ticket.getCategory()),
                ticket.getAssignedTechnician() == null ? null : UserResponse.from(ticket.getAssignedTechnician()));
    }
}

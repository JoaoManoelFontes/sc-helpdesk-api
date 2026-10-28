package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CancelTicketRequest(
        @JsonProperty("solicitanteId") @NotNull @Positive Long requesterId) {
}

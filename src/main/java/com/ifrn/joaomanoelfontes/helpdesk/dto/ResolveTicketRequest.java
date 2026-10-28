package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ResolveTicketRequest(
        @JsonProperty("tecnicoId") @NotNull @Positive Long technicianId,
        @JsonProperty("solucao") @NotBlank @Size(max = 2000) String solution) {
}

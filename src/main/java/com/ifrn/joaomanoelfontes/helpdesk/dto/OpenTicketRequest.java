package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OpenTicketRequest(
        @JsonProperty("titulo") @NotBlank @Size(max = 160) String title,
        @JsonProperty("descricao") @NotBlank @Size(max = 2000) String description,
        @JsonProperty("local") @NotBlank @Size(max = 200) String location,
        @JsonProperty("prioridade") Priority priority,
        @JsonProperty("solicitanteId") @NotNull @Positive Long requesterId,
        @JsonProperty("categoriaId") @NotNull @Positive Long categoryId) {
}

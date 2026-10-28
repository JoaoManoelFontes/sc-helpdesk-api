package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @JsonProperty("nome") @NotBlank @Size(max = 120) String name,
        @JsonProperty("descricao") @NotBlank @Size(max = 500) String description,
        @JsonProperty("setorId") @NotNull @Positive Long sectorId) {
}

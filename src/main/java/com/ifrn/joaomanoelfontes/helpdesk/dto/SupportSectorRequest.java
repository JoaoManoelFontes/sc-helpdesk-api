package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportSectorRequest(
        @JsonProperty("nome") @NotBlank @Size(max = 120) String name,
        @JsonProperty("sigla") @NotBlank @Size(max = 20) String acronym) {
}

package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;

public record SupportSectorResponse(
        Long id,
        @JsonProperty("nome") String name,
        @JsonProperty("sigla") String acronym) {

    public static SupportSectorResponse from(SupportSector sector) {
        return new SupportSectorResponse(sector.getId(), sector.getName(), sector.getAcronym());
    }
}

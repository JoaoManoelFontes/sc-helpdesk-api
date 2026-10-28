package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ifrn.joaomanoelfontes.helpdesk.model.Category;

public record CategoryResponse(
        Long id,
        @JsonProperty("nome") String name,
        @JsonProperty("descricao") String description,
        @JsonProperty("setor") SupportSectorResponse supportSector) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(),
                SupportSectorResponse.from(category.getSupportSector()));
    }
}

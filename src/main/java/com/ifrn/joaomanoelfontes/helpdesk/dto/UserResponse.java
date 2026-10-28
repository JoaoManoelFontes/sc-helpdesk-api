package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ifrn.joaomanoelfontes.helpdesk.model.User;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;

public record UserResponse(
        Long id,
        @JsonProperty("nome") String name,
        String email,
        @JsonProperty("matricula") String registration,
        @JsonProperty("tipo") UserType type,
        @JsonProperty("setor") @JsonInclude(JsonInclude.Include.NON_NULL) SupportSectorResponse supportSector) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRegistration(),
                user.getType(), user.getSupportSector() == null ? null : SupportSectorResponse.from(user.getSupportSector()));
    }
}

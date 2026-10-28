package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ifrn.joaomanoelfontes.helpdesk.model.Interaction;
import java.time.LocalDateTime;

public record InteractionResponse(
        Long id,
        @JsonProperty("texto") String text,
        @JsonProperty("dataHora") LocalDateTime createdAt,
        @JsonProperty("autor") UserResponse author) {

    public static InteractionResponse from(Interaction interaction) {
        return new InteractionResponse(interaction.getId(), interaction.getText(), interaction.getCreatedAt(),
                UserResponse.from(interaction.getAuthor()));
    }
}

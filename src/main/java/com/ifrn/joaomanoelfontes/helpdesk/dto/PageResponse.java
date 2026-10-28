package com.ifrn.joaomanoelfontes.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        @JsonProperty("itens") List<T> items,
        @JsonProperty("pagina") int page,
        @JsonProperty("tamanho") int size,
        @JsonProperty("totalElementos") long totalElements,
        @JsonProperty("totalPaginas") int totalPages) {

    public static <S, T> PageResponse<T> from(Page<S> source, Function<S, T> mapper) {
        return new PageResponse<>(source.getContent().stream().map(mapper).toList(),
                source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages());
    }
}

package com.ifrn.joaomanoelfontes.helpdesk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ifrn.joaomanoelfontes.helpdesk.exception.InvalidRequestException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PaginationSupportTest {

    @Test
    void shouldLimitPageSizeToFifty() {
        var pageable = PaginationSupport.normalize(PageRequest.of(0, 100), Map.of("id", "id"), "id");

        assertThat(pageable.getPageSize()).isEqualTo(50);
    }

    @Test
    void shouldTranslatePortugueseSortProperty() {
        var request = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        var pageable = PaginationSupport.normalize(request, Map.of("dataAbertura", "openedAt"), "openedAt");

        assertThat(pageable.getSort().getOrderFor("openedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void shouldRejectUnknownSortProperty() {
        var request = PageRequest.of(0, 10, Sort.by("inexistente"));

        assertThatThrownBy(() -> PaginationSupport.normalize(request, Map.of("id", "id"), "id"))
                .isInstanceOf(InvalidRequestException.class);
    }
}

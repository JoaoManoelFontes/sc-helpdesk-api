package com.ifrn.joaomanoelfontes.helpdesk.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.Category;
import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;
import com.ifrn.joaomanoelfontes.helpdesk.repository.CategoryRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.SupportSectorRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.TicketRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock SupportSectorRepository sectorRepository;
    @Mock TicketRepository ticketRepository;

    private CategoryService service;
    private Category category;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, sectorRepository, ticketRepository);
        SupportSector sector = new SupportSector("Tecnologia", "COTIC");
        sector.setId(1L);
        category = new Category("Rede", "Problemas de rede", sector);
        category.setId(1L);
    }

    @Test
    void shouldRejectCategoryDeletionWhenTicketExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(ticketRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(BusinessRuleException.class);
        verify(categoryRepository, never()).delete(category);
    }

    @Test
    void shouldDeleteCategoryWithoutTickets() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        service.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldReportMissingSectorWhenListingSectorCategories() {
        when(sectorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listBySector(99L, org.springframework.data.domain.PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.ifrn.joaomanoelfontes.helpdesk.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;
import com.ifrn.joaomanoelfontes.helpdesk.repository.CategoryRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.SupportSectorRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.TicketRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportSectorServiceTest {

    @Mock SupportSectorRepository sectorRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock UserRepository userRepository;
    @Mock TicketRepository ticketRepository;

    private SupportSectorService service;
    private SupportSector sector;

    @BeforeEach
    void setUp() {
        service = new SupportSectorService(sectorRepository, categoryRepository, userRepository, ticketRepository);
        sector = new SupportSector("Tecnologia", "COTIC");
        sector.setId(1L);
    }

    @Test
    void shouldRejectSectorDeletionWhenCategoryExists() {
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(categoryRepository.existsBySupportSectorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(BusinessRuleException.class);
        verify(sectorRepository, never()).delete(sector);
    }

    @Test
    void shouldRejectSectorDeletionWhenTechnicianExists() {
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(userRepository.existsBySupportSectorId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(BusinessRuleException.class);
        verify(sectorRepository, never()).delete(sector);
    }

    @Test
    void shouldDeleteSectorWithoutDependencies() {
        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));

        service.delete(1L);

        verify(sectorRepository).delete(sector);
    }

    @Test
    void shouldReportMissingSector() {
        when(sectorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}

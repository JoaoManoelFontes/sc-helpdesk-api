package com.ifrn.joaomanoelfontes.helpdesk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ifrn.joaomanoelfontes.helpdesk.dto.InteractionRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.OpenTicketRequest;
import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.InvalidRequestException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.Category;
import com.ifrn.joaomanoelfontes.helpdesk.model.Interaction;
import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;
import com.ifrn.joaomanoelfontes.helpdesk.model.Ticket;
import com.ifrn.joaomanoelfontes.helpdesk.model.User;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.Priority;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.TicketStatus;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;
import com.ifrn.joaomanoelfontes.helpdesk.repository.CategoryRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.InteractionRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.TicketRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock InteractionRepository interactionRepository;

    private TicketService service;
    private SupportSector itSector;
    private SupportSector maintenanceSector;
    private Category category;
    private User requester;
    private User otherRequester;
    private User technician;
    private User otherTechnician;

    @BeforeEach
    void setUp() {
        service = new TicketService(ticketRepository, userRepository, categoryRepository, interactionRepository);
        itSector = sector(1L, "COTIC");
        maintenanceSector = sector(2L, "COMAN");
        category = new Category("Rede", "Rede sem acesso", itSector);
        category.setId(1L);
        requester = user(1L, UserType.SOLICITANTE, null);
        otherRequester = user(2L, UserType.SOLICITANTE, null);
        technician = user(3L, UserType.TECNICO, itSector);
        otherTechnician = user(4L, UserType.TECNICO, maintenanceSector);
        lenient().when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            if (ticket.getId() == null) ticket.setId(1L);
            return ticket;
        });
    }

    @Test
    void shouldOpenTicketWithDefaultStatusPriorityAndOpeningDate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        var response = service.open(new OpenTicketRequest("Wi-Fi", "Sem conexão", "Lab 1", null, 1L, 1L));

        assertThat(response.status()).isEqualTo(TicketStatus.ABERTO);
        assertThat(response.priority()).isEqualTo(Priority.MEDIA);
        assertThat(response.openedAt()).isNotNull();
        assertThat(response.assignedTechnician()).isNull();
    }

    @Test
    void shouldPreserveInformedPriority() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        var response = service.open(new OpenTicketRequest("Wi-Fi", "Sem conexão", "Lab 1", Priority.ALTA, 1L, 1L));

        assertThat(response.priority()).isEqualTo(Priority.ALTA);
    }

    @Test
    void shouldRejectTechnicianOpeningTicket() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> service.open(
                new OpenTicketRequest("Wi-Fi", "Sem conexão", "Lab 1", null, 3L, 1L)))
                .isInstanceOf(BusinessRuleException.class);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void shouldReportMissingRequesterWhenOpeningTicket() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.open(
                new OpenTicketRequest("Wi-Fi", "Sem conexão", "Lab 1", null, 99L, 1L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAssignTechnicianFromCategorySector() {
        Ticket ticket = openTicket();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(technician));

        var response = service.assign(1L, 3L);

        assertThat(response.status()).isEqualTo(TicketStatus.EM_ATENDIMENTO);
        assertThat(response.assignedTechnician().id()).isEqualTo(3L);
    }

    @Test
    void shouldRejectTechnicianFromDifferentSector() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket()));
        when(userRepository.findById(4L)).thenReturn(Optional.of(otherTechnician));

        assertThatThrownBy(() -> service.assign(1L, 4L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("setor");
    }

    @Test
    void shouldRejectRequesterTryingToAssignTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> service.assign(1L, 1L)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectAssignmentForNonOpenTicket() {
        Ticket ticket = assignedTicket();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(technician));

        assertThatThrownBy(() -> service.assign(1L, 3L)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldResolveAssignedTicketWithSolutionAndClosingDate() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(assignedTicket()));

        var response = service.resolve(1L, 3L, "Ponto de acesso reiniciado");

        assertThat(response.status()).isEqualTo(TicketStatus.RESOLVIDO);
        assertThat(response.solution()).isEqualTo("Ponto de acesso reiniciado");
        assertThat(response.closedAt()).isNotNull();
    }

    @Test
    void shouldRejectResolutionByDifferentTechnician() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(assignedTicket()));

        assertThatThrownBy(() -> service.resolve(1L, 4L, "Resolvido"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectEmptySolutionInServiceLayer() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(assignedTicket()));

        assertThatThrownBy(() -> service.resolve(1L, 3L, "  "))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectResolutionOutsideInProgressStatus() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket()));

        assertThatThrownBy(() -> service.resolve(1L, 3L, "Resolvido"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldCancelOpenTicketByOriginalRequester() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket()));

        var response = service.cancel(1L, 1L);

        assertThat(response.status()).isEqualTo(TicketStatus.CANCELADO);
    }

    @Test
    void shouldRejectCancellationByDifferentRequester() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket()));

        assertThatThrownBy(() -> service.cancel(1L, otherRequester.getId()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectCancellationAfterAssignment() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(assignedTicket()));

        assertThatThrownBy(() -> service.cancel(1L, 1L)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldAllowRequesterInteractionOnOpenTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(openTicket()));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(invocation -> {
            Interaction interaction = invocation.getArgument(0);
            interaction.setId(1L);
            return interaction;
        });

        var response = service.addInteraction(1L, new InteractionRequest(1L, "Mais detalhes"));

        assertThat(response.text()).isEqualTo("Mais detalhes");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void shouldAllowOnlyAssignedTechnicianInteraction() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(assignedTicket()));
        when(userRepository.findById(4L)).thenReturn(Optional.of(otherTechnician));

        assertThatThrownBy(() -> service.addInteraction(1L, new InteractionRequest(4L, "Teste")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectInteractionOnClosedTicket() {
        Ticket ticket = assignedTicket();
        ticket.setStatus(TicketStatus.RESOLVIDO);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> service.addInteraction(1L, new InteractionRequest(1L, "Teste")))
                .isInstanceOf(BusinessRuleException.class);
        verify(interactionRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvertedOpeningPeriod() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 2, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 1, 0, 0);

        assertThatThrownBy(() -> service.list(null, null, null, null, null, null,
                start, end, PageRequest.of(0, 10)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldCombineTicketFiltersAndReturnEmptyPage() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 31, 23, 59);
        when(ticketRepository.findFiltered(eq(TicketStatus.ABERTO), eq(Priority.ALTA), eq(1L), eq(2L),
                eq(3L), eq(4L), eq(start), eq(end), any())).thenReturn(Page.empty());

        var response = service.list(TicketStatus.ABERTO, Priority.ALTA, 1L, 2L, 3L, 4L,
                start, end, PageRequest.of(2, 10));

        assertThat(response.items()).isEmpty();
        verify(ticketRepository).findFiltered(eq(TicketStatus.ABERTO), eq(Priority.ALTA), eq(1L), eq(2L),
                eq(3L), eq(4L), eq(start), eq(end), any());
    }

    private Ticket openTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Wi-Fi");
        ticket.setDescription("Sem conexão");
        ticket.setLocation("Lab 1");
        ticket.setPriority(Priority.MEDIA);
        ticket.setStatus(TicketStatus.ABERTO);
        ticket.setOpenedAt(LocalDateTime.now());
        ticket.setRequester(requester);
        ticket.setCategory(category);
        return ticket;
    }

    private Ticket assignedTicket() {
        Ticket ticket = openTicket();
        ticket.setAssignedTechnician(technician);
        ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
        return ticket;
    }

    private SupportSector sector(Long id, String acronym) {
        SupportSector sector = new SupportSector(acronym, acronym);
        sector.setId(id);
        return sector;
    }

    private User user(Long id, UserType type, SupportSector sector) {
        User user = new User("Usuário " + id, "u" + id + "@ifrn.edu.br", "M" + id, type, sector);
        user.setId(id);
        return user;
    }
}

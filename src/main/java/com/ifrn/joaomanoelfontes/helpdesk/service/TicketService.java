package com.ifrn.joaomanoelfontes.helpdesk.service;

import com.ifrn.joaomanoelfontes.helpdesk.dto.InteractionRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.InteractionResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.OpenTicketRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.TicketDetailResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.TicketResponse;
import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.InvalidRequestException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.Category;
import com.ifrn.joaomanoelfontes.helpdesk.model.Interaction;
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
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private static final Map<String, String> SORTS = Map.of(
            "id", "id", "titulo", "title", "prioridade", "priority", "status", "status",
            "dataAbertura", "openedAt", "dataFechamento", "closedAt");

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final InteractionRepository interactionRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                         CategoryRepository categoryRepository, InteractionRepository interactionRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.interactionRepository = interactionRepository;
    }

    @Transactional
    public TicketResponse open(OpenTicketRequest request) {
        User requester = requireUser(request.requesterId());
        if (requester.getType() != UserType.SOLICITANTE) {
            throw new BusinessRuleException("Apenas solicitantes podem abrir chamados");
        }
        Category category = requireCategory(request.categoryId());
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setLocation(request.location().trim());
        ticket.setPriority(request.priority() == null ? Priority.MEDIA : request.priority());
        ticket.setStatus(TicketStatus.ABERTO);
        ticket.setOpenedAt(LocalDateTime.now());
        ticket.setRequester(requester);
        ticket.setCategory(category);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> list(TicketStatus status, Priority priority, Long sectorId,
                                             Long categoryId, Long requesterId, Long technicianId,
                                             LocalDateTime startDate, LocalDateTime endDate,
                                             Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidRequestException("dataInicio não pode ser posterior a dataFim");
        }
        Pageable normalized = PaginationSupport.normalize(pageable, SORTS, "openedAt");
        return PageResponse.from(ticketRepository.findFiltered(status, priority, sectorId, categoryId,
                requesterId, technicianId, startDate, endDate, normalized), TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse findById(Long id) {
        Ticket ticket = requireTicket(id);
        var interactions = interactionRepository.findByTicketIdOrderByCreatedAtAsc(id).stream()
                .map(InteractionResponse::from).toList();
        return TicketDetailResponse.from(ticket, interactions);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> listByUser(Long userId, TicketStatus status, Pageable pageable) {
        requireUser(userId);
        Pageable normalized = PaginationSupport.normalize(pageable, SORTS, "openedAt");
        return PageResponse.from(ticketRepository.findByUser(userId, status, normalized), TicketResponse::from);
    }

    @Transactional
    public TicketResponse assign(Long ticketId, Long technicianId) {
        Ticket ticket = requireTicket(ticketId);
        User technician = requireUser(technicianId);
        if (ticket.getStatus() != TicketStatus.ABERTO) {
            throw new BusinessRuleException("Somente chamados abertos podem ser assumidos");
        }
        if (technician.getType() != UserType.TECNICO) {
            throw new BusinessRuleException("Apenas técnicos podem assumir chamados");
        }
        Long technicianSectorId = technician.getSupportSector() == null ? null : technician.getSupportSector().getId();
        if (!Objects.equals(technicianSectorId, ticket.getCategory().getSupportSector().getId())) {
            throw new BusinessRuleException("Técnico não pertence ao setor da categoria");
        }
        ticket.setAssignedTechnician(technician);
        ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse resolve(Long ticketId, Long technicianId, String solution) {
        Ticket ticket = requireTicket(ticketId);
        if (solution == null || solution.isBlank()) {
            throw new BusinessRuleException("A solução é obrigatória");
        }
        if (ticket.getStatus() != TicketStatus.EM_ATENDIMENTO) {
            throw new BusinessRuleException("Somente chamados em atendimento podem ser resolvidos");
        }
        if (ticket.getAssignedTechnician() == null
                || !ticket.getAssignedTechnician().getId().equals(technicianId)) {
            throw new BusinessRuleException("Somente o técnico atribuído pode resolver o chamado");
        }
        ticket.setSolution(solution.trim());
        ticket.setClosedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.RESOLVIDO);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse cancel(Long ticketId, Long requesterId) {
        Ticket ticket = requireTicket(ticketId);
        if (ticket.getStatus() != TicketStatus.ABERTO) {
            throw new BusinessRuleException("Somente chamados abertos podem ser cancelados");
        }
        if (!ticket.getRequester().getId().equals(requesterId)) {
            throw new BusinessRuleException("Somente o solicitante do chamado pode cancelá-lo");
        }
        ticket.setStatus(TicketStatus.CANCELADO);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public InteractionResponse addInteraction(Long ticketId, InteractionRequest request) {
        Ticket ticket = requireTicket(ticketId);
        User author = requireUser(request.userId());
        if (ticket.getStatus() == TicketStatus.RESOLVIDO || ticket.getStatus() == TicketStatus.CANCELADO) {
            throw new BusinessRuleException("Chamados finalizados não aceitam interações");
        }
        boolean requester = ticket.getRequester().getId().equals(author.getId());
        boolean assignedTechnician = ticket.getAssignedTechnician() != null
                && ticket.getAssignedTechnician().getId().equals(author.getId());
        if (!requester && !assignedTechnician) {
            throw new BusinessRuleException("Apenas o solicitante ou o técnico atribuído pode interagir");
        }
        Interaction interaction = new Interaction();
        interaction.setText(request.text().trim());
        interaction.setCreatedAt(LocalDateTime.now());
        interaction.setTicket(ticket);
        interaction.setAuthor(author);
        return InteractionResponse.from(interactionRepository.save(interaction));
    }

    private Ticket requireTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}

package com.ifrn.joaomanoelfontes.helpdesk.controller;

import com.ifrn.joaomanoelfontes.helpdesk.dto.AssignTicketRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.CancelTicketRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.InteractionRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.InteractionResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.OpenTicketRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.ResolveTicketRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.TicketDetailResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.TicketResponse;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.Priority;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.TicketStatus;
import com.ifrn.joaomanoelfontes.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chamados")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> open(@Valid @RequestBody OpenTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.open(request));
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority prioridade,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long solicitanteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 10) Pageable pageable) {
        return service.list(status, prioridade, setorId, categoriaId, solicitanteId, tecnicoId,
                dataInicio, dataFim, pageable);
    }

    @GetMapping("/{id}")
    public TicketDetailResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/assumir")
    public TicketResponse assign(@PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
        return service.assign(id, request.technicianId());
    }

    @PatchMapping("/{id}/resolver")
    public TicketResponse resolve(@PathVariable Long id, @Valid @RequestBody ResolveTicketRequest request) {
        return service.resolve(id, request.technicianId(), request.solution());
    }

    @PatchMapping("/{id}/cancelar")
    public TicketResponse cancel(@PathVariable Long id, @Valid @RequestBody CancelTicketRequest request) {
        return service.cancel(id, request.requesterId());
    }

    @PostMapping("/{id}/interacoes")
    public ResponseEntity<InteractionResponse> addInteraction(@PathVariable Long id,
                                                              @Valid @RequestBody InteractionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addInteraction(id, request));
    }
}

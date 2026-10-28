package com.ifrn.joaomanoelfontes.helpdesk.controller;

import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.TicketResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.UserResponse;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.TicketStatus;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;
import com.ifrn.joaomanoelfontes.helpdesk.service.TicketService;
import com.ifrn.joaomanoelfontes.helpdesk.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;
    private final TicketService ticketService;

    public UserController(UserService userService, TicketService ticketService) {
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public PageResponse<UserResponse> list(@RequestParam(required = false) String nome,
                                           @RequestParam(required = false) UserType tipo,
                                           @RequestParam(required = false) Long setorId,
                                           @PageableDefault(size = 10) Pageable pageable) {
        return userService.list(nome, tipo, setorId, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/{id}/chamados")
    public PageResponse<TicketResponse> listTickets(@PathVariable Long id,
                                                    @RequestParam(required = false) TicketStatus status,
                                                    @PageableDefault(size = 10) Pageable pageable) {
        return ticketService.listByUser(id, status, pageable);
    }
}

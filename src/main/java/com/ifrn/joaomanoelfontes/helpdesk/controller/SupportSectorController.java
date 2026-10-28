package com.ifrn.joaomanoelfontes.helpdesk.controller;

import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.SupportSectorRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.SupportSectorResponse;
import com.ifrn.joaomanoelfontes.helpdesk.service.SupportSectorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setores")
public class SupportSectorController {

    private final SupportSectorService service;

    public SupportSectorController(SupportSectorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SupportSectorResponse> create(@Valid @RequestBody SupportSectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public PageResponse<SupportSectorResponse> list(@RequestParam(required = false) String nome,
                                                    @PageableDefault(size = 10) Pageable pageable) {
        return service.list(nome, pageable);
    }

    @GetMapping("/{id}")
    public SupportSectorResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public SupportSectorResponse update(@PathVariable Long id, @Valid @RequestBody SupportSectorRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.ifrn.joaomanoelfontes.helpdesk.controller;

import com.ifrn.joaomanoelfontes.helpdesk.dto.CategoryRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.CategoryResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.service.CategoryService;
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
@RequestMapping
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/categorias")
    public PageResponse<CategoryResponse> list(@RequestParam(required = false) String nome,
                                               @RequestParam(required = false) Long setorId,
                                               @PageableDefault(size = 10) Pageable pageable) {
        return service.list(nome, setorId, pageable);
    }

    @GetMapping("/categorias/{id}")
    public CategoryResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/setores/{id}/categorias")
    public PageResponse<CategoryResponse> listBySector(@PathVariable Long id,
                                                       @PageableDefault(size = 10) Pageable pageable) {
        return service.listBySector(id, pageable);
    }

    @PutMapping("/categorias/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

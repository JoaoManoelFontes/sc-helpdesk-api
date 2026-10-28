package com.ifrn.joaomanoelfontes.helpdesk.service;

import com.ifrn.joaomanoelfontes.helpdesk.dto.CategoryRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.CategoryResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.Category;
import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;
import com.ifrn.joaomanoelfontes.helpdesk.repository.CategoryRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.SupportSectorRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.TicketRepository;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private static final Map<String, String> SORTS = Map.of("id", "id", "nome", "name", "setorId", "supportSector.id");

    private final CategoryRepository categoryRepository;
    private final SupportSectorRepository sectorRepository;
    private final TicketRepository ticketRepository;

    public CategoryService(CategoryRepository categoryRepository, SupportSectorRepository sectorRepository,
                           TicketRepository ticketRepository) {
        this.categoryRepository = categoryRepository;
        this.sectorRepository = sectorRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        SupportSector sector = requireSector(request.sectorId());
        return CategoryResponse.from(categoryRepository.save(
                new Category(request.name().trim(), request.description().trim(), sector)));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> list(String name, Long sectorId, Pageable pageable) {
        if (sectorId != null) {
            requireSector(sectorId);
        }
        Pageable normalized = PaginationSupport.normalize(pageable, SORTS, "name");
        return PageResponse.from(categoryRepository.findFiltered(blankToNull(name), sectorId, normalized),
                CategoryResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> listBySector(Long sectorId, Pageable pageable) {
        requireSector(sectorId);
        Pageable normalized = PaginationSupport.normalize(pageable, SORTS, "name");
        return PageResponse.from(categoryRepository.findBySupportSectorId(sectorId, normalized), CategoryResponse::from);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(requireCategory(id));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = requireCategory(id);
        category.setName(request.name().trim());
        category.setDescription(request.description().trim());
        category.setSupportSector(requireSector(request.sectorId()));
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = requireCategory(id);
        if (ticketRepository.existsByCategoryId(id)) {
            throw new BusinessRuleException("Categoria possui chamados vinculados");
        }
        categoryRepository.delete(category);
    }

    private Category requireCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    private SupportSector requireSector(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

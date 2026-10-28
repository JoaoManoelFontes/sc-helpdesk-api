package com.ifrn.joaomanoelfontes.helpdesk.service;

import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.SupportSectorRequest;
import com.ifrn.joaomanoelfontes.helpdesk.dto.SupportSectorResponse;
import com.ifrn.joaomanoelfontes.helpdesk.exception.BusinessRuleException;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;
import com.ifrn.joaomanoelfontes.helpdesk.repository.CategoryRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.SupportSectorRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.TicketRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.UserRepository;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportSectorService {

    private static final Map<String, String> SORTS = Map.of("id", "id", "nome", "name", "sigla", "acronym");

    private final SupportSectorRepository sectorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public SupportSectorService(SupportSectorRepository sectorRepository, CategoryRepository categoryRepository,
                                UserRepository userRepository, TicketRepository ticketRepository) {
        this.sectorRepository = sectorRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public SupportSectorResponse create(SupportSectorRequest request) {
        checkAcronym(request.acronym(), null);
        return SupportSectorResponse.from(sectorRepository.save(
                new SupportSector(request.name().trim(), request.acronym().trim().toUpperCase())));
    }

    @Transactional(readOnly = true)
    public PageResponse<SupportSectorResponse> list(String name, Pageable pageable) {
        Pageable normalized = PaginationSupport.normalize(pageable, SORTS, "name");
        Page<SupportSector> result = name == null || name.isBlank()
                ? sectorRepository.findAll(normalized)
                : sectorRepository.findByNameContainingIgnoreCase(name.trim(), normalized);
        return PageResponse.from(result, SupportSectorResponse::from);
    }

    @Transactional(readOnly = true)
    public SupportSectorResponse findById(Long id) {
        return SupportSectorResponse.from(requireSector(id));
    }

    @Transactional
    public SupportSectorResponse update(Long id, SupportSectorRequest request) {
        SupportSector sector = requireSector(id);
        checkAcronym(request.acronym(), id);
        sector.setName(request.name().trim());
        sector.setAcronym(request.acronym().trim().toUpperCase());
        return SupportSectorResponse.from(sectorRepository.save(sector));
    }

    @Transactional
    public void delete(Long id) {
        SupportSector sector = requireSector(id);
        if (categoryRepository.existsBySupportSectorId(id)
                || userRepository.existsBySupportSectorId(id)
                || ticketRepository.existsByCategorySupportSectorId(id)
                || ticketRepository.existsByAssignedTechnicianSupportSectorId(id)) {
            throw new BusinessRuleException("Setor possui categorias, técnicos ou chamados vinculados");
        }
        sectorRepository.delete(sector);
    }

    private SupportSector requireSector(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado"));
    }

    private void checkAcronym(String acronym, Long currentId) {
        boolean duplicate = currentId == null
                ? sectorRepository.existsByAcronymIgnoreCase(acronym.trim())
                : sectorRepository.existsByAcronymIgnoreCaseAndIdNot(acronym.trim(), currentId);
        if (duplicate) {
            throw new BusinessRuleException("Já existe um setor com esta sigla");
        }
    }
}

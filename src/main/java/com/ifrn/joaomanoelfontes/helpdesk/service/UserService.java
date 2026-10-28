package com.ifrn.joaomanoelfontes.helpdesk.service;

import com.ifrn.joaomanoelfontes.helpdesk.dto.PageResponse;
import com.ifrn.joaomanoelfontes.helpdesk.dto.UserResponse;
import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;
import com.ifrn.joaomanoelfontes.helpdesk.repository.SupportSectorRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.UserRepository;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Map<String, String> SORTS = Map.of(
            "id", "id", "nome", "name", "email", "email", "matricula", "registration", "tipo", "type");

    private final UserRepository userRepository;
    private final SupportSectorRepository sectorRepository;

    public UserService(UserRepository userRepository, SupportSectorRepository sectorRepository) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(String name, UserType type, Long sectorId, Pageable pageable) {
        if (sectorId != null && !sectorRepository.existsById(sectorId)) {
            throw new ResourceNotFoundException("Setor não encontrado");
        }
        Pageable normalized = PaginationSupport.normalize(pageable, SORTS, "name");
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        return PageResponse.from(userRepository.findFiltered(normalizedName, type, sectorId, normalized), UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado")));
    }
}

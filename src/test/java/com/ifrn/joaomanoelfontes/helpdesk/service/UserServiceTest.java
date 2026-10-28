package com.ifrn.joaomanoelfontes.helpdesk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.ifrn.joaomanoelfontes.helpdesk.exception.ResourceNotFoundException;
import com.ifrn.joaomanoelfontes.helpdesk.model.User;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;
import com.ifrn.joaomanoelfontes.helpdesk.repository.SupportSectorRepository;
import com.ifrn.joaomanoelfontes.helpdesk.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock SupportSectorRepository sectorRepository;

    private UserService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, sectorRepository);
        user = new User("Ana", "ana@ifrn.edu.br", "20260001", UserType.SOLICITANTE, null);
        user.setId(1L);
    }

    @Test
    void shouldFindSeedUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(service.findById(1L).name()).isEqualTo("Ana");
    }

    @Test
    void shouldReportMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldListUsersWithCombinedFilters() {
        when(sectorRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findFiltered(org.mockito.ArgumentMatchers.eq("Ana"),
                org.mockito.ArgumentMatchers.eq(UserType.SOLICITANTE), org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(user)));

        var response = service.list("Ana", UserType.SOLICITANTE, 1L, PageRequest.of(0, 10));

        assertThat(response.items()).hasSize(1);
    }
}

package com.ifrn.joaomanoelfontes.helpdesk.repository;

import com.ifrn.joaomanoelfontes.helpdesk.model.SupportSector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportSectorRepository extends JpaRepository<SupportSector, Long> {

    Page<SupportSector> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByAcronymIgnoreCase(String acronym);

    boolean existsByAcronymIgnoreCaseAndIdNot(String acronym, Long id);
}

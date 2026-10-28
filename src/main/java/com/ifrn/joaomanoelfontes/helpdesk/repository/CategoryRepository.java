package com.ifrn.joaomanoelfontes.helpdesk.repository;

import com.ifrn.joaomanoelfontes.helpdesk.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            SELECT c FROM Category c
            WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%'))
              AND (:sectorId IS NULL OR c.supportSector.id = :sectorId)
            """)
    Page<Category> findFiltered(@Param("name") String name, @Param("sectorId") Long sectorId,
                                Pageable pageable);

    Page<Category> findBySupportSectorId(Long sectorId, Pageable pageable);

    boolean existsBySupportSectorId(Long sectorId);
}

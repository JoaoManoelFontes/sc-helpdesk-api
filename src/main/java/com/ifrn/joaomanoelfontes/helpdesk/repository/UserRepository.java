package com.ifrn.joaomanoelfontes.helpdesk.repository;

import com.ifrn.joaomanoelfontes.helpdesk.model.User;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', COALESCE(:name, ''), '%'))
              AND (:type IS NULL OR u.type = :type)
              AND (:sectorId IS NULL OR u.supportSector.id = :sectorId)
            """)
    Page<User> findFiltered(@Param("name") String name, @Param("type") UserType type,
                            @Param("sectorId") Long sectorId, Pageable pageable);

    boolean existsBySupportSectorId(Long sectorId);
}

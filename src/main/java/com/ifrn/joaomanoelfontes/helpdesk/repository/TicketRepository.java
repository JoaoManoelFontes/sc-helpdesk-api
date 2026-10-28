package com.ifrn.joaomanoelfontes.helpdesk.repository;

import com.ifrn.joaomanoelfontes.helpdesk.model.Ticket;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.Priority;
import com.ifrn.joaomanoelfontes.helpdesk.model.enums.TicketStatus;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
            SELECT t FROM Ticket t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:sectorId IS NULL OR t.category.supportSector.id = :sectorId)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:requesterId IS NULL OR t.requester.id = :requesterId)
              AND (:technicianId IS NULL OR t.assignedTechnician.id = :technicianId)
              AND t.openedAt >= COALESCE(:startDate, t.openedAt)
              AND t.openedAt <= COALESCE(:endDate, t.openedAt)
            """)
    Page<Ticket> findFiltered(
            @Param("status") TicketStatus status,
            @Param("priority") Priority priority,
            @Param("sectorId") Long sectorId,
            @Param("categoryId") Long categoryId,
            @Param("requesterId") Long requesterId,
            @Param("technicianId") Long technicianId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
            SELECT t FROM Ticket t
            WHERE (t.requester.id = :userId OR t.assignedTechnician.id = :userId)
              AND (:status IS NULL OR t.status = :status)
            """)
    Page<Ticket> findByUser(@Param("userId") Long userId, @Param("status") TicketStatus status,
                            Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByCategorySupportSectorId(Long sectorId);

    boolean existsByAssignedTechnicianSupportSectorId(Long sectorId);
}

package com.ifrn.joaomanoelfontes.helpdesk.repository;

import com.ifrn.joaomanoelfontes.helpdesk.model.Interaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}

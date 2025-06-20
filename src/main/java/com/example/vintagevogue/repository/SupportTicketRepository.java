package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.SupportTicket;
import com.example.vintagevogue.model.TicketStatus;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUser(User user);
    List<SupportTicket> findByStatus(TicketStatus status);
}

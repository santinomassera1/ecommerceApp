package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.SupportTicket;
import com.example.vintagevogue.model.TicketResponse;
import com.example.vintagevogue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketResponseRepository extends JpaRepository<TicketResponse, Long> {
    List<TicketResponse> findByTicket(SupportTicket ticket);
    List<TicketResponse> findByTicketOrderByCreatedAtAsc(SupportTicket ticket);
    List<TicketResponse> findByUser(User user);
} 
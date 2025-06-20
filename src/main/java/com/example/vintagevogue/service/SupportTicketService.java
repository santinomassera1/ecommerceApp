package com.example.vintagevogue.service;

import com.example.vintagevogue.model.SupportTicket;
import com.example.vintagevogue.model.TicketStatus;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    public List<SupportTicket> getTicketsByUser(User user) {
        return supportTicketRepository.findByUser(user);
    }

    public List<SupportTicket> getAllTickets() {
        return supportTicketRepository.findAll();
    }

    public SupportTicket createTicket(SupportTicket ticket) {
        return supportTicketRepository.save(ticket);
    }

    public Optional<SupportTicket> getTicketById(Long id) {
        return supportTicketRepository.findById(id);
    }

    public SupportTicket updateStatus(Long id, TicketStatus status) {
        Optional<SupportTicket> ticket = supportTicketRepository.findById(id);
        if (ticket.isPresent()) {
            SupportTicket updatedTicket = ticket.get();
            updatedTicket.setStatus(status);
            if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
                updatedTicket.setResolvedAt(java.time.LocalDateTime.now());
            }
            return supportTicketRepository.save(updatedTicket);
        }
        return null;
    }

    public void deleteTicket(Long id) {
        supportTicketRepository.deleteById(id);
    }
}

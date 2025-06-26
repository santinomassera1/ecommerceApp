package com.example.vintagevogue.service;

import com.example.vintagevogue.model.SupportTicket;
import com.example.vintagevogue.model.TicketResponse;
import com.example.vintagevogue.model.TicketStatus;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.TicketResponseRepository;
import com.example.vintagevogue.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketResponseService {

    @Autowired
    private TicketResponseRepository ticketResponseRepository;
    
    @Autowired
    private SupportTicketRepository supportTicketRepository;

    public List<TicketResponse> getResponsesByTicket(SupportTicket ticket) {
        return ticketResponseRepository.findByTicketOrderByCreatedAtAsc(ticket);
    }

    public TicketResponse addResponse(SupportTicket ticket, User user, String content, boolean isFromSupport) {
        TicketResponse response = new TicketResponse(ticket, user, content, isFromSupport);
        
        // Si es una respuesta de soporte, actualizar el estado del ticket a "En Progreso"
        if (isFromSupport && ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            supportTicketRepository.save(ticket);
        }
        
        return ticketResponseRepository.save(response);
    }

    public Optional<TicketResponse> getResponseById(Long id) {
        return ticketResponseRepository.findById(id);
    }

    public void deleteResponse(Long id) {
        ticketResponseRepository.deleteById(id);
    }
} 
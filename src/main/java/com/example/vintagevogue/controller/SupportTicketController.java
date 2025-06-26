package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.SupportTicket;
import com.example.vintagevogue.model.TicketResponse;
import com.example.vintagevogue.model.TicketStatus;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.SupportTicketService;
import com.example.vintagevogue.service.TicketResponseService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/support")
@PreAuthorize("hasRole('SUPP')")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private TicketResponseService ticketResponseService;

    // Mostrar la página principal con los tickets del usuario
    @GetMapping
    public String viewSupportTickets(Model model, Authentication authentication) {
        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            List<SupportTicket> tickets = supportTicketService.getTicketsByUser(user.get());
            model.addAttribute("tickets", tickets);
        }
        return "support/support"; // Redirige a support/index.html
    }

    // Mostrar el formulario para crear un nuevo ticket
    @GetMapping("/new")
    public String newTicketForm(Model model) {
        model.addAttribute("ticket", new SupportTicket());
        model.addAttribute("categories", new String[]{"Producto", "Envío", "Pago", "Cuenta", "Otro"});
        return "support/new"; // Redirige a support/new.html
    }

    // Manejar la creación de un nuevo ticket
    @PostMapping("/create")
    public String createTicket(@ModelAttribute SupportTicket ticket, Authentication authentication) {
        Optional<User> user = userService.findByUsername(authentication.getName());
        if (user.isPresent()) {
            ticket.setUser(user.get());
            ticket.setStatus(TicketStatus.OPEN);
            supportTicketService.createTicket(ticket);
        }
        return "redirect:/support";
    }

    // Ver detalles de un ticket específico
    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(id);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            model.addAttribute("ticket", ticket);
            
            // Obtener las respuestas del ticket
            List<TicketResponse> responses = ticketResponseService.getResponsesByTicket(ticket);
            model.addAttribute("responses", responses);
            
            // Añadir un objeto para la nueva respuesta
            model.addAttribute("newResponse", new TicketResponse());
            
            // Añadir el usuario actual para verificar si es soporte
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean isSupport = user.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_SUPP"));
                model.addAttribute("isSupport", isSupport);
                model.addAttribute("currentUser", user);
            }
            
            return "support/view"; // Redirige a support/view.html
        }
        return "redirect:/support";
    }

    @PostMapping("/update-status")
    public String updateTicketStatus(@RequestParam Long ticketId, @RequestParam TicketStatus status) {
        supportTicketService.updateStatus(ticketId, status);
        return "redirect:/support/" + ticketId;
    }
    
    @PostMapping("/{id}/respond")
    public String addResponse(@PathVariable Long id, @RequestParam String content, Authentication authentication) {
        Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(id);
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        
        if (ticketOpt.isPresent() && userOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            User user = userOpt.get();
            
            // Determinar si la respuesta es de soporte
            boolean isFromSupport = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPP") || a.getAuthority().equals("ROLE_ADMIN"));
            
            // Añadir la respuesta
            ticketResponseService.addResponse(ticket, user, content, isFromSupport);
        }
        
        return "redirect:/support/" + id;
    }
    
    @PostMapping("/{id}/priority")
    public String updatePriority(@PathVariable Long id, @RequestParam Integer priority) {
        Optional<SupportTicket> ticketOpt = supportTicketService.getTicketById(id);
        if (ticketOpt.isPresent()) {
            SupportTicket ticket = ticketOpt.get();
            ticket.setPriority(priority);
            supportTicketService.createTicket(ticket);
        }
        return "redirect:/support/" + id;
    }
}

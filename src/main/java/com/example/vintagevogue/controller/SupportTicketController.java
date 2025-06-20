package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.SupportTicket;
import com.example.vintagevogue.model.TicketStatus;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.SupportTicketService;
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
@PreAuthorize("hasRole('SUPPORT')")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserService userService;

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
    public String viewTicket(@PathVariable Long id, Model model) {
        Optional<SupportTicket> ticket = supportTicketService.getTicketById(id);
        if (ticket.isPresent()) {
            model.addAttribute("ticket", ticket.get());
            return "support/view"; // Redirige a support/view.html
        }
        return "redirect:/support";
    }

    @PostMapping("/update-status")
    public String updateTicketStatus(@RequestParam Long ticketId, @RequestParam TicketStatus status) {
        supportTicketService.updateStatus(ticketId, status);
        return "redirect:/support/" + ticketId;
    }
}

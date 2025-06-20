package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Order;
import com.example.vintagevogue.model.OrderStatus;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.OrderService;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * Página principal de seguimiento - formulario para ingresar número de tracking
     */
    @GetMapping("/track")
    public String showTrackingForm() {
        return "tracking/track-form";
    }

    /**
     * Mostrar seguimiento de pedido por número de tracking
     */
    @GetMapping("/track/{trackingNumber}")
    public String trackOrder(@PathVariable String trackingNumber, Model model) {
        Optional<Order> orderOpt = orderService.findByTrackingNumber(trackingNumber);
        
        if (orderOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró ningún pedido con el número de seguimiento: " + trackingNumber);
            return "tracking/track-form";
        }
        
        Order order = orderOpt.get();
        model.addAttribute("order", order);
        model.addAttribute("trackingNumber", trackingNumber);
        
        return "tracking/track-details";
    }

    /**
     * Buscar pedido por número de tracking (POST)
     */
    @PostMapping("/track")
    public String searchOrder(@RequestParam("trackingNumber") String trackingNumber, 
                             RedirectAttributes redirectAttributes) {
        if (trackingNumber == null || trackingNumber.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor ingresa un número de seguimiento");
            return "redirect:/orders/track";
        }
        
        return "redirect:/orders/track/" + trackingNumber.trim().toUpperCase();
    }

    /**
     * Mostrar pedidos del usuario logueado
     */
    @GetMapping("/my-orders")
    public String showMyOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        
        List<Order> orders = orderService.getUserOrders(user);
        model.addAttribute("orders", orders);
        
        return "tracking/my-orders";
    }

    /**
     * Mostrar detalles de una orden específica del usuario
     */
    @GetMapping("/details/{orderId}")
    public String showOrderDetails(@PathVariable Long orderId, Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        
        // Buscar la orden y verificar que pertenece al usuario
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            model.addAttribute("error", "Orden no encontrada");
            return "redirect:/orders/my-orders";
        }
        
        Order order = orderOpt.get();
        // Verificar que la orden pertenece al usuario
        if (!order.getUser().getId().equals(user.getId())) {
            model.addAttribute("error", "No tienes permisos para ver esta orden");
            return "redirect:/orders/my-orders";
        }
        
        model.addAttribute("order", order);
        model.addAttribute("trackingNumber", order.getTrackingNumber());
        
        return "tracking/track-details";
    }

    /**
     * Panel de administración - mostrar todas las órdenes
     */
    @GetMapping("/admin")
    public String showAdminPanel(Model model) {
        List<Order> orders = orderService.getAllOrders();
        OrderService.OrderStatistics stats = orderService.getOrderStatistics();
        
        model.addAttribute("orders", orders);
        model.addAttribute("stats", stats);
        model.addAttribute("statuses", OrderStatus.values());
        
        return "admin/orders-management";
    }

    /**
     * Actualizar estado de una orden (Admin)
     */
    @PostMapping("/admin/update-status")
    public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                   @RequestParam("status") OrderStatus status,
                                   @RequestParam(value = "notes", required = false) String notes,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(orderId, status, notes);
            redirectAttributes.addFlashAttribute("success", "Estado de la orden actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado: " + e.getMessage());
        }
        
        return "redirect:/orders/admin";
    }

    /**
     * Filtrar órdenes por estado (Admin)
     */
    @GetMapping("/admin/filter")
    public String filterOrdersByStatus(@RequestParam("status") OrderStatus status, Model model) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        OrderService.OrderStatistics stats = orderService.getOrderStatistics();
        
        model.addAttribute("orders", orders);
        model.addAttribute("stats", stats);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        
        return "admin/orders-management";
    }
} 
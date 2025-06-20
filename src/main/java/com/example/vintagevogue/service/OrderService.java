package com.example.vintagevogue.service;

import com.example.vintagevogue.model.*;
import com.example.vintagevogue.repository.OrderRepository;
import com.example.vintagevogue.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Crear una nueva orden a partir de los items del carrito
     */
    @Transactional
    public Order createOrderFromCart(User user, List<CartItem> cartItems, String shippingAddress) {
        // Calcular el total
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear la orden
        Order order = new Order(user, totalAmount);
        order.setShippingAddress(shippingAddress);
        // Establecer como CONFIRMED ya que el pago fue procesado
        order.setStatus(OrderStatus.CONFIRMED);

        // Convertir CartItems a OrderItems
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getProduct().getPrice()
            );
            order.addItem(orderItem);
        }

        // Guardar la orden
        Order savedOrder = orderRepository.save(order);

        // Enviar email de confirmación con link de seguimiento
        sendOrderConfirmationEmail(savedOrder);

        return savedOrder;
    }

    /**
     * Crear una orden para compra instantánea
     */
    @Transactional
    public Order createInstantOrder(User user, Product product, String shippingAddress) {
        Order order = new Order(user, product.getPrice());
        order.setShippingAddress(shippingAddress);
        // Establecer como CONFIRMED ya que el pago fue procesado
        order.setStatus(OrderStatus.CONFIRMED);

        OrderItem orderItem = new OrderItem(product, 1, product.getPrice());
        order.addItem(orderItem);

        Order savedOrder = orderRepository.save(order);
        sendOrderConfirmationEmail(savedOrder);

        return savedOrder;
    }

    /**
     * Buscar orden por número de seguimiento
     */
    @Transactional(readOnly = true)
    public Optional<Order> findByTrackingNumber(String trackingNumber) {
        Optional<Order> orderOpt = orderRepository.findByTrackingNumber(trackingNumber);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Inicializar el usuario
            order.getUser().getUsername();
            // Inicializar las colecciones lazy
            order.getItems().size();
            order.getItems().forEach(item -> {
                item.getProduct().getName();
                if (item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        }
        return orderOpt;
    }

    /**
     * Buscar orden por ID
     */
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Inicializar el usuario
            order.getUser().getUsername();
            // Inicializar las colecciones lazy
            order.getItems().size();
            order.getItems().forEach(item -> {
                item.getProduct().getName();
                if (item.getProduct().getImages() != null) {
                    item.getProduct().getImages().size();
                }
            });
        }
        return orderOpt;
    }

    /**
     * Obtener todas las órdenes de un usuario
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrders(User user) {
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        // Inicializar las colecciones lazy para evitar LazyInitializationException
        orders.forEach(order -> {
            order.getItems().size(); // Esto fuerza la inicialización de la colección
            order.getItems().forEach(item -> {
                item.getProduct().getName(); // Inicializa el producto también
            });
        });
        return orders;
    }

    /**
     * Obtener todas las órdenes (para admin)
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAllOrdersByDateDesc();
        // Inicializar las colecciones lazy
        orders.forEach(order -> {
            order.getItems().size();
            order.getItems().forEach(item -> {
                item.getProduct().getName();
            });
        });
        return orders;
    }

    /**
     * Actualizar el estado de una orden
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String notes) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Orden no encontrada: " + orderId);
        }

        Order order = orderOpt.get();
        // Inicializar el usuario para evitar LazyInitializationException
        order.getUser().getUsername();
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        if (notes != null && !notes.trim().isEmpty()) {
            order.setNotes(notes);
        }

        Order updatedOrder = orderRepository.save(order);

        // Enviar notificación de cambio de estado
        sendStatusUpdateEmail(updatedOrder, oldStatus, newStatus);

        return updatedOrder;
    }

    /**
     * Obtener órdenes por estado
     */
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatusOrderByOrderDateDesc(status);
        // Inicializar las colecciones lazy
        orders.forEach(order -> {
            order.getItems().size();
            order.getItems().forEach(item -> {
                item.getProduct().getName();
            });
        });
        return orders;
    }

    /**
     * Obtener estadísticas de órdenes
     */
    public OrderStatistics getOrderStatistics() {
        OrderStatistics stats = new OrderStatistics();
        stats.setPendingCount(orderRepository.countByStatus(OrderStatus.PENDING));
        stats.setConfirmedCount(orderRepository.countByStatus(OrderStatus.CONFIRMED));
        stats.setShippedCount(orderRepository.countByStatus(OrderStatus.SHIPPED));
        stats.setDeliveredCount(orderRepository.countByStatus(OrderStatus.DELIVERED));
        stats.setCancelledCount(orderRepository.countByStatus(OrderStatus.CANCELLED));
        stats.setTotalCount(orderRepository.count());
        return stats;
    }

    /**
     * Enviar email de confirmación de orden
     */
    private void sendOrderConfirmationEmail(Order order) {
        String subject = "Confirmación de Pedido - Vintage Vogue";
        
        StringBuilder body = new StringBuilder();
        body.append("Estimado/a ").append(order.getUser().getUsername()).append(",\n\n");
        body.append("¡Gracias por tu compra en Vintage Vogue!\n\n");
        body.append("✅ Tu pago ha sido confirmado exitosamente.\n\n");
        body.append("Detalles de tu pedido:\n");
        body.append("Número de seguimiento: ").append(order.getTrackingNumber()).append("\n");
        body.append("Total: $").append(order.getTotalAmount()).append("\n");
        body.append("Estado: ").append(order.getStatusDescription()).append("\n\n");
        
        body.append("Productos:\n");
        for (OrderItem item : order.getItems()) {
            body.append("- ").append(item.getProduct().getName())
                .append(" x").append(item.getQuantity())
                .append(" - $").append(item.getTotalPrice()).append("\n");
        }
        
        body.append("\n🚚 Puedes hacer seguimiento de tu pedido en el siguiente enlace:\n");
        body.append("http://localhost:8080/orders/track/").append(order.getTrackingNumber()).append("\n\n");
        
        body.append("Te notificaremos sobre cualquier cambio en el estado de tu envío.\n\n");
        body.append("Saludos,\nEquipo de Vintage Vogue");

        emailService.sendSimpleEmail(order.getUser().getEmail(), subject, body.toString());
    }

    /**
     * Enviar email de actualización de estado
     */
    private void sendStatusUpdateEmail(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        String subject = "Actualización de Pedido - " + order.getTrackingNumber();
        
        StringBuilder body = new StringBuilder();
        body.append("Estimado/a ").append(order.getUser().getUsername()).append(",\n\n");
        body.append("Tu pedido ").append(order.getTrackingNumber()).append(" ha sido actualizado.\n\n");
        body.append("Estado anterior: ").append(oldStatus.getDisplayName()).append("\n");
        body.append("Estado actual: ").append(newStatus.getDisplayName()).append("\n\n");
        
        // Mensajes personalizados según el estado
        switch (newStatus) {
            case CONFIRMED:
                body.append("¡Excelente! Hemos confirmado tu pedido y estamos preparándolo para el envío.\n");
                break;
            case PREPARING:
                body.append("Estamos preparando tu pedido con mucho cuidado.\n");
                break;
            case SHIPPED:
                body.append("¡Tu pedido ya está en camino! Pronto lo tendrás contigo.\n");
                break;
            case IN_TRANSIT:
                body.append("Tu pedido está siendo transportado hacia ti.\n");
                break;
            case OUT_FOR_DELIVERY:
                body.append("¡Tu pedido está en tu zona y será entregado hoy!\n");
                break;
            case DELIVERED:
                body.append("¡Tu pedido ha sido entregado exitosamente! Esperamos que disfrutes tu compra.\n");
                break;
            case CANCELLED:
                body.append("Tu pedido ha sido cancelado. Si tienes preguntas, no dudes en contactarnos.\n");
                break;
        }
        
        if (order.getNotes() != null && !order.getNotes().trim().isEmpty()) {
            body.append("\nNotas adicionales: ").append(order.getNotes()).append("\n");
        }
        
        body.append("\n🚚 Puedes hacer seguimiento completo en:\n");
        body.append("http://localhost:8080/orders/track/").append(order.getTrackingNumber()).append("\n\n");
        
        body.append("Saludos,\nEquipo de Vintage Vogue");

        emailService.sendSimpleEmail(order.getUser().getEmail(), subject, body.toString());
    }

    /**
     * Clase interna para estadísticas
     */
    public static class OrderStatistics {
        private Long totalCount;
        private Long pendingCount;
        private Long confirmedCount;
        private Long shippedCount;
        private Long deliveredCount;
        private Long cancelledCount;

        // Getters y setters
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        
        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }
        
        public Long getConfirmedCount() { return confirmedCount; }
        public void setConfirmedCount(Long confirmedCount) { this.confirmedCount = confirmedCount; }
        
        public Long getShippedCount() { return shippedCount; }
        public void setShippedCount(Long shippedCount) { this.shippedCount = shippedCount; }
        
        public Long getDeliveredCount() { return deliveredCount; }
        public void setDeliveredCount(Long deliveredCount) { this.deliveredCount = deliveredCount; }
        
        public Long getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(Long cancelledCount) { this.cancelledCount = cancelledCount; }
    }
} 
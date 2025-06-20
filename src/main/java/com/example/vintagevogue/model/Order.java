package com.example.vintagevogue.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String trackingNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    @Column
    private LocalDateTime shippedDate;
    
    @Column
    private LocalDateTime deliveredDate;
    
    @Column(length = 500)
    private String shippingAddress;
    
    @Column(length = 1000)
    private String notes;
    
    // Constructor por defecto
    public Order() {
        this.trackingNumber = generateTrackingNumber();
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }
    
    // Constructor con parámetros básicos
    public Order(User user, BigDecimal totalAmount) {
        this();
        this.user = user;
        this.totalAmount = totalAmount;
    }
    
    private String generateTrackingNumber() {
        return "VV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
        
        // Actualizar fechas automáticamente según el estado
        if (status == OrderStatus.SHIPPED && this.shippedDate == null) {
            this.shippedDate = LocalDateTime.now();
        } else if (status == OrderStatus.DELIVERED && this.deliveredDate == null) {
            this.deliveredDate = LocalDateTime.now();
        }
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDateTime getShippedDate() {
        return shippedDate;
    }
    
    public void setShippedDate(LocalDateTime shippedDate) {
        this.shippedDate = shippedDate;
    }
    
    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }
    
    public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Método helper para agregar items
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    // Método helper para verificar si el pedido está completado
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED;
    }
    
    // Método helper para obtener descripción del estado
    public String getStatusDescription() {
        return status.getDisplayName();
    }
} 
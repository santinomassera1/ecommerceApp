package com.example.vintagevogue.model;

public enum OrderStatus {
    PENDING("Pendiente"),
    CONFIRMED("Confirmado"),
    PREPARING("Preparando envío"),
    SHIPPED("Enviado"),
    IN_TRANSIT("En tránsito"),
    OUT_FOR_DELIVERY("Fuera para entrega"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 
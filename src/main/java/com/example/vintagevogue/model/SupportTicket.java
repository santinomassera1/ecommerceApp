package com.example.vintagevogue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime resolvedAt;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketResponse> responses = new ArrayList<>();

    @Column
    private Integer priority = 2; // 1: Alta, 2: Media, 3: Baja

    @Column
    private String category;

    public SupportTicket() {
        this.createdAt = LocalDateTime.now();
        this.status = TicketStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public List<TicketResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<TicketResponse> responses) {
        this.responses = responses;
    }
    
    public void addResponse(TicketResponse response) {
        responses.add(response);
        response.setTicket(this);
    }

    public void removeResponse(TicketResponse response) {
        responses.remove(response);
        response.setTicket(null);
    }
    
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

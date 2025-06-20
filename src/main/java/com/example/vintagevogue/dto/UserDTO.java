package com.example.vintagevogue.dto;

import com.example.vintagevogue.model.User;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String address;
    private String city;
    private String country;
    private List<String> roles; // Incluimos los roles del usuario

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.city = user.getCity();
        this.country = user.getCountry();
        this.roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()); // Convertimos roles a una lista de strings
    }
    // 🔹 Agregar un constructor vacío para la deserialización de Jackson
    public UserDTO() {
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}

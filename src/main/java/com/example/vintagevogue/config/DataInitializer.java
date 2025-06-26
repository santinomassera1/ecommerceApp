package com.example.vintagevogue.config;

import com.example.vintagevogue.model.Role;
import com.example.vintagevogue.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar roles si no existen
        createRoleIfNotFound("ROLE_USER");
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_SUPP");
    }

    private void createRoleIfNotFound(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            roleRepository.save(role);
            System.out.println("Rol creado: " + name);
        }
    }
} 
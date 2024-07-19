package com.example.vintagevogue.repository;

import com.example.vintagevogue.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
<<<<<<< HEAD
}
=======
}
>>>>>>> fb5bd05981b960023b76268448945f182399185d

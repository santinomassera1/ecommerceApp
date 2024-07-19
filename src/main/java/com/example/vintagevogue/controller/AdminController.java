package com.example.vintagevogue.controller;

import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/role-assignment")
    public String roleAssignmentPage(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", userService.findAllRoles());
        return "role-assignment";
    }

    @PostMapping("/assign-role")
    @ResponseBody
    public ResponseEntity<String> assignRole(@RequestParam String username, @RequestParam String roleName) {
        boolean success = userService.assignRoleToUser(username, roleName);
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Role assigned successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error assigning role\"}");
        }
    }

<<<<<<< HEAD
}
=======
}
>>>>>>> fb5bd05981b960023b76268448945f182399185d

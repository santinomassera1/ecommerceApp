package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.User;
import com.example.vintagevogue.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/assign-role")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> assignRole(@RequestParam String username, @RequestParam String role) {
        userService.assignRoleToUser(username, role);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Role assigned successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping("/block-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> blockUser(@RequestParam String username) {
        userService.blockUser(username);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User blocked successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam String username) {
        userService.deleteUser(username);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public String adminHome() {
        return "admin";
    }
}
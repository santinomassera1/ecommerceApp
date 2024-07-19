package com.example.vintagevogue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
public class SupportController {

    @GetMapping
    public String supportPage() {
        return "support";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> submitIssue(@RequestBody SupportRequest request) {
        // Aquí puedes manejar la lógica para registrar el problema, por ejemplo, enviarlo a una base de datos o un sistema de tickets.
        boolean success = handleSupportRequest(request.getUsername(), request.getIssue());
        if (success) {
            return ResponseEntity.ok("{\"message\":\"Issue submitted successfully\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"message\":\"Error submitting issue\"}");
        }
    }

    private boolean handleSupportRequest(String username, String issue) {
        // Implementa la lógica para manejar el problema reportado por el usuario
        // Aquí puedes enviar el problema a un sistema de tickets o almacenarlo en una base de datos
        return true; // Cambia esto según la lógica implementada
    }

    // Clase interna para recibir los datos del formulario
    public static class SupportRequest {
        private String username;
        private String issue;

        // Getters y setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getIssue() {
            return issue;
        }

        public void setIssue(String issue) {
            this.issue = issue;
        }
    }
}

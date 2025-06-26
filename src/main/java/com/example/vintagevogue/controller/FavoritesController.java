package com.example.vintagevogue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    @GetMapping
    public String showFavorites() {
        return "favorites";
    }
} 
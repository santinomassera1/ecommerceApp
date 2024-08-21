package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.service.AdService;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private AdService adService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;
    @GetMapping("/home")
    public String home(Model model) {
        List<Ad> ads = adService.getAllAds();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        ads.forEach(ad -> {
            if (ad.getStartDate() != null) {
                String formattedDate = ad.getStartDate().format(formatter);
                ad.setFormattedDate(formattedDate);
            }
        });

        model.addAttribute("ads", ads);

        List<Product> products = productService.getAllProductsWithUsers();
        model.addAttribute("products", products);

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "home";
    }
}

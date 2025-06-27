package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.service.AdService;
import com.example.vintagevogue.service.CategoryService;
import com.example.vintagevogue.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${app.maintenance.enabled:false}")
    private boolean maintenanceMode;
    
    @Value("${app.maintenance.message:La aplicación se encuentra en mantenimiento. Por favor, intente más tarde.}")
    private String maintenanceMessage;
    
    @GetMapping("/home")
    public String home(Model model) {
        // Verificar si la aplicación está en modo mantenimiento
        if (maintenanceMode) {
            model.addAttribute("maintenanceMessage", maintenanceMessage);
            return "maintenance";
        }
        
        List<Ad> ads = adService.getAllAds();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        ads.forEach(ad -> {
            if (ad.getStartDate() != null) {
                String formattedDate = ad.getStartDate().format(formatter);
                ad.setFormattedDate(formattedDate);
            }
        });

        model.addAttribute("ads", ads);

        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "home";
    }
    
    @GetMapping("/")
    public String root() {
        // Verificar si la aplicación está en modo mantenimiento
        if (maintenanceMode) {
            return "redirect:/home";
        }
        return "redirect:/home";
    }
}

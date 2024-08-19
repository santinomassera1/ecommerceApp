package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/ads")
public class AdController {

    @Autowired
    private AdService adService;

    @GetMapping
    public String manageAds(Model model) {
        model.addAttribute("ads", adService.getAllAds());
        model.addAttribute("ad", new Ad());
        return "add-management";
    }

    @PostMapping("/save")
    public String saveAd(@ModelAttribute Ad ad) {
        adService.saveAd(ad);
        return "redirect:/admin/ads";
    }

    @GetMapping("/edit/{id}")
    public String editAd(@PathVariable Long id, Model model) {
        Ad ad = adService.getAdById(id).orElseThrow(() -> new IllegalArgumentException("Invalid ad Id: " + id));
        model.addAttribute("ad", ad);
        model.addAttribute("ads", adService.getAllAds());
        return "add-management";
    }

    @GetMapping("/delete/{id}")
    public String deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return "redirect:/admin/ads";
    }
}

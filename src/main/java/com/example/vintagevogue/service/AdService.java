package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Ad;
import com.example.vintagevogue.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdService {

    @Autowired
    private AdRepository adRepository;

    public List<Ad> getAllAds() {
        return adRepository.findAll();
    }

    public Optional<Ad> getAdById(Long id) {
        return adRepository.findById(id);
    }

    public void saveAd(Ad ad) {
        adRepository.save(ad);
    }

    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }
}

package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Image;
import com.example.vintagevogue.repository.ImageRepository;
import com.example.vintagevogue.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ImageService imageService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Image image = imageService.getImage(id);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(image.getImageType()))
                .body(image.getImageData());
    }
}

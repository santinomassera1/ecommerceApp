package com.example.vintagevogue.controller;

import com.example.vintagevogue.model.Image;
import com.example.vintagevogue.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @GetMapping("/image/{productId}")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable Long productId) {
        Optional<Image> imageOptional = imageRepository.findByProductId(productId);

        if (imageOptional.isPresent()) {
            Image image = imageOptional.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(image.getImageType()))
                    .body(image.getImageData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Image;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    public Image saveImage(MultipartFile file, Product product) throws IOException {
        Image image = new Image();
        image.setImageData(file.getBytes());
        image.setImageName(file.getOriginalFilename());
        image.setImageType(file.getContentType());
        image.setProduct(product);
        imageRepository.save(image);
        return image;
    }
    public Image getImage(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
    }
}

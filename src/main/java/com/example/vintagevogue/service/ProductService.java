package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Image;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    public List<Product> getAllProductsWithUsers() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public void saveProduct(Product product, MultipartFile[] imageFiles) throws IOException {
        Product savedProduct = productRepository.save(product);

        List<Image> images = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                Image image = imageService.saveImage(imageFile, savedProduct);
                images.add(image);
            }
        }
        savedProduct.setImages(images);
        productRepository.save(savedProduct);
    }

    public List<Product> getProductsByUser(User user) {
        return productRepository.findByUser(user);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

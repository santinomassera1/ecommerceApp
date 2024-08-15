package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    public String saveProductImage(MultipartFile file, Long productId) {
        // Aquí procesamos el archivo y lo guardamos en el servidor
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uploadDir = "user-photos/";

        try {
            // Guardar la imagen en el servidor
            ImageStorageService.saveFile(uploadDir, fileName, file);
        } catch (IOException e) {
            // Manejar error
            throw new RuntimeException("Error guardando la imagen", e);
        }

        // Crear la URL de la imagen
        String imageUrl = uploadDir + fileName;

        // Actualizar el producto con la URL de la imagen
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        product.setImageUrl(imageUrl);
        productRepository.save(product);

        return imageUrl;
    }

    public List<Product> getAllProductsWithUsers() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }
    public List<Product> getProductsByUser(User user) {
        return productRepository.findByUser(user);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}

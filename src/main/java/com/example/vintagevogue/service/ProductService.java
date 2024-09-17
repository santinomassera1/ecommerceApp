package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.Image;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.ProductRepository;
import com.example.vintagevogue.repository.CategoryRepository;
import com.example.vintagevogue.repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public void saveProduct(Product product, MultipartFile[] imageFiles) throws IOException {
        // Cargar categorías desde la base de datos para evitar problemas con entidades detached
        Set<Category> managedCategories = new HashSet<>();
        for (Category category : product.getCategories()) {
            if (category.getId() != null) {
                Category managedCategory = categoryRepository.findById(category.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + category.getId()));
                managedCategories.add(managedCategory);
            } else {
                // Persistir nueva categoría
                categoryRepository.save(category);
                managedCategories.add(category);
            }
        }
        product.setCategories(managedCategories);

        // Guardar el producto primero en la base de datos
        Product savedProduct = productRepository.save(product);

        List<Image> images = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                // Guardar la imagen en la carpeta y generar la URL
                Image image = saveImage(imageFile, savedProduct);
                images.add(image);
            }
        }

        // Asignar la primera imagen como la imagen principal del producto si hay imágenes
        if (!images.isEmpty()) {
            savedProduct.setImageUrl(images.get(0).getUrl());
        }

        // Guardar todas las imágenes asociadas al producto
        savedProduct.setImages(images);

        // Guardar nuevamente el producto con todas las imágenes
        productRepository.save(savedProduct);
    }

    @Transactional
    public void saveProductWithoutImages(Product product) {
        // Cargar categorías desde la base de datos
        Set<Category> managedCategories = new HashSet<>();
        for (Category category : product.getCategories()) {
            if (category.getId() != null) {
                Category managedCategory = categoryRepository.findById(category.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + category.getId()));
                managedCategories.add(managedCategory);
            } else {
                // Persistir nueva categoría
                categoryRepository.save(category);
                managedCategories.add(category);
            }
        }
        product.setCategories(managedCategories);

        // Guardar el producto sin imágenes
        productRepository.save(product);
    }

    @Transactional
    public void saveImagesForProduct(Long productId, MultipartFile[] imageFiles) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productId));

        // En lugar de crear una nueva lista, usa la lista existente
        List<Image> existingImages = product.getImages();
        
        for (MultipartFile imageFile : imageFiles) {
            if (!imageFile.isEmpty()) {
                Image image = saveImage(imageFile, product);
                existingImages.add(image);  // Añadir la imagen a la lista existente
            }
        }

        // No necesitas reasignar la colección de imágenes, ya que la has modificado directamente
        productRepository.save(product);  // Guardar el producto con las imágenes añadidas
    }

    private Image saveImage(MultipartFile imageFile, Product product) throws IOException {
        // Definir el nombre único para la imagen
        String imageName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();

        // Definir la ruta donde se guardará la imagen
        Path imagePath = Paths.get("src/main/resources/static/images", imageName);

        // Copiar el archivo a la carpeta
        Files.copy(imageFile.getInputStream(), imagePath);

        // Crear y guardar la entidad Image
        Image image = new Image();
        image.setUrl("/images/" + imageName); 
        image.setProduct(product);

        // Guardar la entidad Image en la base de datos
        return imageRepository.save(image);
    }

    public List<Product> getProductsByUser(User user) {
        return productRepository.findByUser(user);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
    
        if (!productOptional.isPresent()) {
            throw new IllegalStateException("Product not found");
        }
    
        Product product = productOptional.get();
    
        // 1. Limpiar la relación con las categorías
        product.getCategories().clear();
        productRepository.save(product);  // Guardar el producto después de limpiar las categorías
    
        // 2. Limpiar la relación con las imágenes (esto ya debería estar manejado por orphanRemoval = true)
        for (Image image : product.getImages()) {
            imageRepository.delete(image);  // Eliminar cada imagen asociada
        }
        product.getImages().clear();  // Limpiar la lista de imágenes
    
        // 3. Eliminar el producto
        productRepository.delete(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}

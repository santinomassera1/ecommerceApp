package com.example.vintagevogue.service;

import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ProductService {

    private static final Logger logger = Logger.getLogger(ProductService.class.getName());

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Product addProduct(Product product) {
        logger.info("Adding product: " + product.getName());
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        logger.info("Getting product by ID: " + id);
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.info("Getting all products");
        return productRepository.findAll();
    }

    @Transactional
    public Product updateProduct(Product product) {
        logger.info("Updating product: " + product.getName());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Deleting product by ID: " + id);
        productRepository.deleteById(id);
    }

}


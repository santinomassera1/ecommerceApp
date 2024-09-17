package com.example.vintagevogue.service;

import com.example.vintagevogue.dto.SearchResultDto;
import com.example.vintagevogue.model.Category;
import com.example.vintagevogue.model.Product;
import com.example.vintagevogue.model.User;
import com.example.vintagevogue.repository.CategoryRepository;
import com.example.vintagevogue.repository.ProductRepository;
import com.example.vintagevogue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<SearchResultDto> search(String keyword) {
        return getSearchResultDtos(keyword);
    }

    private List<SearchResultDto> getSearchResultDtos(String keyword) {
        List<SearchResultDto> results = new ArrayList<>();

        // Buscar productos que coincidan con el keyword
        List<Product> products = productRepository.findByNameContaining(keyword);
        for (Product product : products) {
            // No incluimos imágenes, solo los datos necesarios
            results.add(new SearchResultDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(), "product", null));
        }

        // Buscar usuarios que coincidan con el keyword
        List<User> users = userRepository.findByRoles_NameAndUsernameContaining("ROLE_USER", keyword);
        for (User user : users) {
            results.add(new SearchResultDto(user.getId(), user.getUsername(), user.getEmail(), null, "user", null));
        }

        // Buscar categorías que coincidan con el keyword
        List<Category> categories = categoryRepository.findByNameContaining(keyword);
        for (Category category : categories) {
            results.add(new SearchResultDto(category.getId(), category.getName(), null, null, "category", null));
        }

        return results;
    }

    public List<SearchResultDto> getSuggestions(String keyword) {
        return getSearchResultDtos(keyword);
    }
}

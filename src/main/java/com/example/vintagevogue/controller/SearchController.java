package com.example.vintagevogue.controller;

import com.example.vintagevogue.dto.SearchResultDto;
import com.example.vintagevogue.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResultDto>> search(@RequestParam String query) {
        List<SearchResultDto> results = searchService.search(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<SearchResultDto>> getSuggestions(@RequestParam String query) {
        List<SearchResultDto> suggestions = searchService.getSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

}

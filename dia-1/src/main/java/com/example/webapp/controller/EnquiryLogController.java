package com.example.webapp.controller;

import com.example.webapp.service.LogService;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class EnquiryLogController {

    private final LogService service;

    // static category data for UI — move to DB if needed
    private static final Map<Integer, String> CATEGORY_MAP = Map.of(
            1, "Diamond",
            2, "Open Setting",
            3, "Plain Gold",
            4, "Vilandi",
            5, "Jadtar"
    );

    private static final Map<String, List<Map<String, Object>>> SUB_CATEGORIES = new HashMap<>();
    static {
        SUB_CATEGORIES.put("Jadtar", List.of(
                Map.of("id", 6, "name", "Jadtar Register"),
                Map.of("id", 7, "name", "Jadtar Halfsets"),
                Map.of("id", 8, "name", "Jadtar Bangles / Bracelets"),
                Map.of("id", 17, "name", "Only Earrings")
        ));
        SUB_CATEGORIES.put("Vilandi", List.of(
                Map.of("id", 9, "name", "Vilandi Halfsets"),
                Map.of("id", 10, "name", "Vilandi Bangles / Bracelets")
        ));
        SUB_CATEGORIES.put("Diamond", List.of(
                Map.of("id", 20, "name", "Diamond Rings"),
                Map.of("id", 19, "name", "Diamond Earrings"),
                Map.of("id", 11, "name", "Diamond Bangles / Bracelets"),
                Map.of("id", 12, "name", "Diamond Pendants / Pendant Sets"),
                Map.of("id", 13, "name", "Diamond Halfsets")
        ));
        SUB_CATEGORIES.put("Open Setting", List.of(
                Map.of("id", 14, "name", "OS Halfsets"),
                Map.of("id", 15, "name", "OS Bangles / Bracelets")
        ));
        SUB_CATEGORIES.put("Plain Gold", List.of(
                Map.of("id", 18, "name", "Chains"),
                Map.of("id", 16, "name", "PG Bangles / Bracelets")
        ));
    }

    public EnquiryLogController(LogService service) {
        this.service = service;
    }

    @GetMapping("/enquiries")
    public String enquiriesPage() {
        return "forward:/index.html";
    }

    /**
     * JSON API for data table: supports q, categoryId, subCategoryId, page, size, sort
     */
    @GetMapping("/api/enquiries")
    @ResponseBody
    public Map<String, Object> apiEnquiries(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "subCategoryId", required = false) Integer subCategoryId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "createdAt,desc") String sort
    ) {
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sorting);
        var result = service.searchEnquiries(q, categoryId, subCategoryId, pageable);

        Map<String, Object> resp = new HashMap<>();
        resp.put("content", result.getContent());
        resp.put("page", result.getNumber());
        resp.put("size", result.getSize());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        return resp;
    }
    
    @DeleteMapping("/api/enquiries/{id}")
    public ResponseEntity<Void> deleteEnquiry(@PathVariable Integer id) {
    	service.deleteEnquiry(id);
        return ResponseEntity.noContent().build();
    }


    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        try {
            String[] parts = sortParam.split(",");
            String prop = parts[0];
            Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            return Sort.by(dir, prop);
        } catch (Exception ex) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}

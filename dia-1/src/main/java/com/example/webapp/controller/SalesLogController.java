package com.example.webapp.controller;

import com.example.webapp.entity.SalesLog;
import com.example.webapp.repository.SalesLogRepository;
import com.example.webapp.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SalesLogController {

    @Autowired
    private SalesLogRepository salesLogRepository;

    @GetMapping("/api/sales-logs")
    @ResponseBody
    public Page<SalesLog> fetchSalesLogs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryIdStr,
            @RequestParam(required = false) String subCategoryIdStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return salesLogRepository.searchWithJsonFilters(q, categoryIdStr, subCategoryIdStr, pageable);
    }
}
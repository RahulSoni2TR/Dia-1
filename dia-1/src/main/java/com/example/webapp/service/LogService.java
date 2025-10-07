// LogService.java
package com.example.webapp.service;

import com.example.webapp.models.LogRequest;
import com.example.webapp.models.Product;
import com.example.webapp.entity.EnquiryLog;
import com.example.webapp.entity.SalesLog;
import com.example.webapp.repository.EnquiryLogRepository;
import com.example.webapp.repository.OrderRepository;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.SalesLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {

	@Autowired
    private  EnquiryLogRepository enquiryLogRepository;
	
	@Autowired
    private  SalesLogRepository salesLogRepository;
	
	@Autowired
	private  ProductRepository productRepository;
	
	@Autowired
	private  OrderRepository ordersRepository;
	
	@Autowired
	private ObjectMapper objectMapper;


	 @Transactional
	    public void logEnquiry(LogRequest request) {
		 Product product = productRepository.findById(request.getProductId())
			      .orElseThrow(() -> new RuntimeException("Product not found"));
		 //	System.out.println("request is "+request);
			  EnquiryLog log = new EnquiryLog();
			  log.setProductId(product.getProductId());
			  log.setDesignNo(product.getDesignNo());
			  log.setCustomerName(request.getCustomerName());
			  log.setImageUrl(product.getImageUrl());
			  log.setCreatedAt(LocalDateTime.now());

			  // new: explicit prices from request
			  log.setPrice(request.getPrice());
			  log.setPriceWithFields(request.getPriceWithFields());

			  try {
			    // existing frozen product snapshot
			    log.setProductSnapshot(objectMapper.writeValueAsString(product));
			    // new: store complete estimate snapshot if present
			    if (request.getEstimateSnapshot() != null) {
			      log.setEstimateSnapshot(objectMapper.writeValueAsString(request.getEstimateSnapshot()));
			    }
			  } catch (JsonProcessingException e) {
			    throw new RuntimeException("Failed to serialize snapshots", e);
			  }

			  enquiryLogRepository.save(log);
	    }
   
	 @Transactional
	    public void logSale(LogRequest request) {
	        Product product = productRepository.findById(request.getProductId())
	                .orElseThrow(() -> new RuntimeException("Product not found"));

	        SalesLog log = new SalesLog();
	        log.setProductId(product.getProductId());
	        log.setDesignNo(product.getDesignNo());
	        log.setOrderId(request.getOrderId());
	        log.setCustomerName(request.getCustomerName());
	        log.setImageUrl(product.getImageUrl());
	        log.setCreatedAt(LocalDateTime.now());
	        log.setPrice(request.getPrice());
	        log.setPriceWithFields(request.getPriceWithFields());
	        try {
	            log.setProductSnapshot(objectMapper.writeValueAsString(product));
	            if (request.getEstimateSnapshot() != null) {
				      log.setEstimateSnapshot(objectMapper.writeValueAsString(request.getEstimateSnapshot()));
				    }
	        } catch (JsonProcessingException e) {
	            throw new RuntimeException("Failed to serialize product", e);
	        }

	        salesLogRepository.save(log);

	        // free product mapping
	        productRepository.resetProductMappingByDesignNo(request.getDesignNo());

	        // clear reverse mapping in orders table
	        if (request.getOrderId() != null) {
	            ordersRepository.resetOrderMapping(request.getOrderId());
	        }
	     //   productRepository.save(product);
	    }
	 
	 public Page<EnquiryLog> searchEnquiries(
	            String q,
	            Integer categoryId,
	            Integer subCategoryId,
	            Pageable pageable) {

	        // Try DB-level JSON filtered search (MySQL JSON)
	        try {
	            String categoryIdStr = categoryId == null ? null : String.valueOf(categoryId);
	            String subCategoryIdStr = subCategoryId == null ? null : String.valueOf(subCategoryId);

	            Page<EnquiryLog> page = enquiryLogRepository.searchWithJsonFilters(
	                    (q == null || q.isBlank()) ? null : q.trim(),
	                    categoryIdStr,
	                    subCategoryIdStr,
	                    pageable
	            );
	            return page;
	        } catch (Exception ex) {
	            // If DB doesn't support JSON functions or native query fails, fall back to safer approach
	            // Fetch a larger page and filter in memory (not ideal for huge datasets).
	            // We first attempt a JPA search by q, otherwise fetch all pages and filter.
	            Pageable fetchPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
	            Page<EnquiryLog> initial = (q == null || q.isBlank())
	                    ? enquiryLogRepository.findAll(fetchPage)
	                    : enquiryLogRepository.findByDesignNoContainingIgnoreCaseOrCustomerNameContainingIgnoreCase(q, q, fetchPage);

	            if (categoryId == null && subCategoryId == null) {
	                return initial;
	            }

	            // filter on parsed JSON fields in productSnapshot
	            List<EnquiryLog> filtered = initial.getContent().stream()
	                    .filter(e -> snapshotMatches(e.getProductSnapshot(), categoryId, subCategoryId))
	                    .collect(Collectors.toList());

	            return new PageImpl<>(filtered, pageable, filtered.size());
	        }
	    }

	    // Utility: checks JSON string (basic) for categoryId/subCategoryId values.
	    // Uses naive substring checks for speed; for robust solution parse JSON (e.g., Jackson).
	    private boolean snapshotMatches(String snapshotJson, Integer categoryId, Integer subCategoryId) {
	        if ((categoryId == null) && (subCategoryId == null)) return true;
	        if (snapshotJson == null || snapshotJson.isBlank()) return false;

	        // A safer approach would be to parse JSON using Jackson and read fields
	        try {
	            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
	            Map<String, Object> map = mapper.readValue(snapshotJson, Map.class);

	            if (categoryId != null) {
	                Object cat = map.get("categoryId");
	                if (cat == null) return false;
	                // compare as number or string
	                if (!categoryId.toString().equals(String.valueOf(cat))) return false;
	            }
	            if (subCategoryId != null) {
	                Object sub = map.get("subCategoryId");
	                if (sub == null) return false;
	                if (!subCategoryId.toString().equals(String.valueOf(sub))) return false;
	            }
	            return true;
	        } catch (Exception ex) {
	            // if parsing fails, fallback to simple contains check
	            if (categoryId != null && !snapshotJson.contains("\"categoryId\":" + categoryId)) return false;
	            if (subCategoryId != null && !snapshotJson.contains("\"subCategoryId\":" + subCategoryId)) return false;
	            return true;
	        }
	    }

//	    public Page<SalesLog> getSalesLogs(String q, String categoryId, String subCategoryId, int page, int size, String sortBy) {
//	    	Pageable pageable = PageRequest.of(page, size, Sort.by("created_at").descending());
//
//	        return salesLogRepository.searchSalesLogs(
//	                (q != null && !q.isEmpty()) ? q : null,
//	                (categoryId != null && !categoryId.isEmpty()) ? categoryId : null,
//	                (subCategoryId != null && !subCategoryId.isEmpty()) ? subCategoryId : null,
//	                pageable
//	        );
//	    }
//	    
//	    // Helper to fetch a single enquiry (optional)
//	    public Optional<EnquiryLog> findById(Long id) {
//	        return enquiryLogRepository.findById(id);
//	    }
}

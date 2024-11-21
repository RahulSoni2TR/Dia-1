package com.example.webapp.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.webapp.models.Estimate;
import com.example.webapp.models.Product;
import com.example.webapp.models.ProductPage;
import com.example.webapp.service.ProductService;
import com.example.webapp.models.Rate;

@Controller
public class ProductController {
	
	
	@Autowired
    private ProductService productService;

    // Only logged-in users can add products
	@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/add-product")
    public String showAddProductPage() {
       
        return "add_product";
    }
    
	@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/remove-product")
    public String removeProductPage() {
    	System.out.println("came inside remove");
        return "remove_product"; // Return the remove_product.html view
    }

	@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/modify-product")
    public String modifyProductPage() {
        return "modify_product"; // Return the modify_product.html view
    }
    
	@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/set-price")
    public String setPricePage() {
        return "set_price"; // Return the modify_product.html view
    }
	
	
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view-product")
    public String viewProductPage() {
        return "view_products"; // Return the view_product.html view
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/load-product")
    public String loadPoductPage() {
        return "load_product"; // Return the view_product.html view
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-estimate")
    public String getEstimatePage() {
        return "get_estimate"; // Return the view_product.html view
    }
    
    
    
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("imageUrl") MultipartFile imageFile,

            // Category-specific fields
            @RequestParam(value = "net", required = false) BigDecimal net,
            @RequestParam(value = "pcs", required = false) Integer pcs,
            @RequestParam(value = "diaWeight", required = false) BigDecimal diaWeight,
            @RequestParam(value = "remarks", required = false) String remarks,
            
            @RequestParam(value = "gross", required = false) BigDecimal gross,
            @RequestParam(value = "vilandiCt", required = false) BigDecimal vilandiCt,
            @RequestParam(value = "diamondsCt", required = false) BigDecimal diamondsCt,
            @RequestParam(value = "beadsCt", required = false) BigDecimal beadsCt,
            @RequestParam(value = "pearlsGm", required = false) BigDecimal pearlsGm,
            @RequestParam(value = "otherStonesCt", required = false) BigDecimal otherStonesCt,
            @RequestParam(value = "others", required = false) String others,
            
            @RequestParam(value = "designNo", required = false) String designNo,

            @RequestParam(value = "earringNet", required = false) BigDecimal earringNet,
            @RequestParam(value = "earringPcs", required = false) Integer earringPcs,
            @RequestParam(value = "diamondWeightEarring", required = false) BigDecimal diamondWeightEarring,
            @RequestParam(value = "vilandi", required = false) BigDecimal vilandi,
            @RequestParam(value = "vilandiRate", required = false) BigDecimal vilandiRate,
            @RequestParam(value = "stones", required = false) BigDecimal stones,
            @RequestParam(value = "vilandiStoneRate", required = false) BigDecimal vilandiStoneRate,
            @RequestParam(value = "beadsVilandi", required = false) BigDecimal beadsVilandi,
            @RequestParam(value = "vilandiBeadsRate", required = false) BigDecimal vilandiBeadsRate,
            @RequestParam(value = "pearlsVilandi", required = false) BigDecimal pearlsVilandi,
            @RequestParam(value = "vilandiPearlRate", required = false) BigDecimal vilandiPearlRate,
            @RequestParam(value = "ssPearlCt", required = false) BigDecimal ssPearlCt,
            @RequestParam(value = "vilandiSSPearlRate", required = false) BigDecimal vilandiSSPearlRate,
            @RequestParam(value = "realStone", required = false) BigDecimal realStone,
            @RequestParam(value = "vilandiFitting", required = false) BigDecimal vilandiFitting,
            @RequestParam(value = "jadtarGross", required = false) BigDecimal jadtarGross,
            @RequestParam(value = "jadtarNet", required = false) BigDecimal jadtarNet,
            @RequestParam(value = "jadtarStones", required = false) BigDecimal jadtarStones,
            @RequestParam(value = "jadtarStoneRate", required = false) BigDecimal jadtarStoneRate,
            @RequestParam(value = "jadtarBeads", required = false) BigDecimal jadtarBeads,
            @RequestParam(value = "jadtarBeadsRate", required = false) BigDecimal jadtarBeadsRate,
            @RequestParam(value = "jadtarPearls", required = false) BigDecimal jadtarPearls,
            @RequestParam(value = "jadtarPearlRate", required = false) BigDecimal jadtarPearlsRate,
            @RequestParam(value = "jadtarSSPearl", required = false) BigDecimal jadtarSSPearl,
            @RequestParam(value = "jadtarSSPearlRate", required = false) BigDecimal jadtarSSPearlRate,
            @RequestParam(value = "jadtarRealStone", required = false) BigDecimal jadtarRealStone,
            @RequestParam(value = "jadtarFitting", required = false) BigDecimal jadtarFittingRate
            ) {
    	
        // Process the image file
        String imageUrl = saveImageFile(imageFile);

        // Create a Product entity from the parameters
        Product product = new Product();
        product.setItem(productName);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setCategoryId(categoryId);
        product.setImageUrl(imageUrl);// Set the image URL
        product.setRemarks(remarks);

        // Set category-specific fields based on categoryId
        if (categoryId == 1) { // Diamond Rings
            product.setNet(net);
            product.setPcs(pcs);
            product.setDiaWeight(diaWeight);
        } else if (categoryId == 2) { // Open Setting
            product.setGross(gross);
            product.setNet(net);
            product.setVilandiCt(vilandiCt);
            product.setvRate(vilandiRate);
            product.setDiamondsCt(diamondsCt);
            product.setBeadsCt(beadsCt);
            product.setBdRate(vilandiBeadsRate);
            product.setPearlsGm(pearlsGm);
            product.setPrlRate(vilandiPearlRate);
            product.setOtherStonesCt(otherStonesCt);
        } else if (categoryId == 3) { // Chains
            product.setDesignNo(designNo);
            product.setNet(net);
        } else if (categoryId == 4) { // Diamond Earrings
            product.setDesignNo(designNo);
            product.setNet(earringNet);
            product.setPcs(earringPcs);
            product.setDiaWeight(diamondWeightEarring);
        } else if (categoryId == 5) { // Vilandi
            product.setDesignNo(designNo);
            product.setNet(net);
            product.setVilandiCt(vilandi);
            product.setvRate(vilandiRate);
            product.setStones(stones);
            product.setStRate(vilandiStoneRate);
            product.setBeadsCt(beadsVilandi);
            product.setBdRate(vilandiBeadsRate);
            product.setPearlsGm(pearlsVilandi);
            product.setPrlRate(vilandiPearlRate);
            product.setSsPearlCt(ssPearlCt);
            product.setSsRate(vilandiSSPearlRate);
            product.setRealStone(realStone);
            product.setFitting(vilandiFitting);
        } else if (categoryId == 6) { // Jadtar Register
            product.setDesignNo(designNo);
            product.setGross(jadtarGross);
            product.setNet(jadtarNet);
            product.setStones(jadtarStoneRate);
            product.setStRate(jadtarRealStone);
            product.setBeadsCt(jadtarBeads);
            product.setBdRate(jadtarBeadsRate);
            product.setPearlsGm(jadtarPearls);
            product.setPrlRate(jadtarPearlsRate);
            product.setSsPearlCt(jadtarSSPearl);
            product.setSsRate(jadtarSSPearlRate);
            product.setRealStone(jadtarRealStone);
            product.setFitting(jadtarFittingRate);
        }

        // Add the product using the service
        productService.addProduct(product);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product added successfully!");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }


    private String saveImageFile(MultipartFile imageFile) {
        try {
            // Specify the directory to save the images
            String uploadDir = "C:\\Users\\Admin\\Downloads\\uploads\\"; // Update to your desired upload directory
            Files.createDirectories(Paths.get(uploadDir));

            // Define the path where the file will be saved
            Path filePath = Paths.get(uploadDir + imageFile.getOriginalFilename());

            // Save the file
            imageFile.transferTo(filePath.toFile());

            // Construct a URL to access the image
            String imageUrl = "http://localhost:8081/" + imageFile.getOriginalFilename(); // Adjust based on your context
            return imageUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @PostMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable Long productId) {
        productService.removeProduct(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product deleted successfully!");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.findProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("loadProduct/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Optional<Product> product = productService.findProductById(id);
        return product.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/load")
    public ResponseEntity<ProductPage> loadProducts(@RequestParam int page, 
                                                    @RequestParam int size, 
                                                    @RequestParam(required = false) Integer category) {
        Page<Product> products;
        if (category == null) {
            products = productService.findAll(PageRequest.of(page, size));
        } else {
            products = productService.findByCategory(category, PageRequest.of(page, size));
        }
        return ResponseEntity.ok(new ProductPage(products.getContent(), products.getTotalElements()));
    }
    
    @PutMapping(value="/updateProduct/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @ModelAttribute Product updatedProduct, @RequestParam("image") MultipartFile imageFile) {
        Product updated = productService.updateProduct(id, updatedProduct,imageFile);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/getEstimate/{productId}")
    public ResponseEntity< Map<String, Object>> getEstimate(@PathVariable Long productId) {
    	  Product product = productService.findProductById(productId)
                  .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    	  List<Rate> rates = productService.getAllRates();
    	   BigDecimal goldPrice = null;
    	     BigDecimal diamondPrice = null;
    	      BigDecimal vilandiPrice = null;
    	      BigDecimal gst = null;
    	     for (Rate rate : rates) {
    	            switch (rate.getCommodity().toLowerCase()) {
    	                case "gold":
    	                    goldPrice = rate.getPrice();
    	                    break;
    	                case "diamond":
    	                    diamondPrice = rate.getPrice();
    	                    break;
    	                case "vilandi":
    	                    vilandiPrice = rate.getPrice();
    	                    break;
    	                default:
    	                    gst = rate.getPrice();
    	                    break;
    	            }
    	     }
    	  Estimate e = new Estimate();
    	  if (product.getCategoryId() == 6) {
    		    BigDecimal labour = product.getNet().multiply(BigDecimal.valueOf(1100));
    		    e.setLabour(labour);

    		    BigDecimal gold1 = goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP);
    		 //   System.out.println(gold1);

    		    e.setGold(nullSafe(product.getNet()).multiply(gold1));
    		    e.setStones(nullSafe(product.getStones()).multiply(nullSafe(product.getStRate())));
    		    e.setBeads(nullSafe(product.getBeadsCt()).multiply(nullSafe(product.getBdRate())));
    		    e.setPearls(nullSafe(product.getPearlsGm()).multiply(nullSafe(product.getPrlRate())));
    		    e.setSsPearls(nullSafe(product.getSsPearlCt()).multiply(nullSafe(product.getSsRate())));
    		    e.setMozo(nullSafe(product.getMozonite()).multiply(nullSafe(product.getMozonite())));  // update
    		    e.setRealStones(nullSafe(product.getRealStone()));
    		    e.setFitting(nullSafe(product.getFitting()));

    		    BigDecimal estimate_nogst = e.getGold()
    		            .add(e.getStones())
    		            .add(e.getBeads())
    		            .add(e.getPearls())
    		            .add(e.getSsPearls())
    		            .add(e.getMozo())
    		            .add(e.getRealStones())
    		            .add(e.getFitting());

    		    BigDecimal estimate_gst = estimate_nogst.multiply(gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
    		    e.setGst(estimate_gst);

    		    BigDecimal total = estimate_nogst.add(estimate_gst);
    		    e.setTotal(total);
    		}
    	  Map<String, Object> response = new HashMap<>();
    	  response.put("object1", product);
    	  response.put("object2", e);
    	  response.put("object3",rates);
System.out.println("reutn esitmate");
    	  return ResponseEntity.ok(response);

     //   return ResponseEntity.ok(e);
  
    }
    
    @GetMapping("/prices")
    public ResponseEntity<List<Rate>> getAllRates() {
        List<Rate> rates = productService.getAllRates();
        return ResponseEntity.ok(rates);
    }

    @PostMapping("/updatePrices")
    public ResponseEntity<String> updatePrices(@RequestBody Map<String, BigDecimal> prices) {
        String response = productService.updatePrices(prices);
        return ResponseEntity.ok(response);
    }
    
    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

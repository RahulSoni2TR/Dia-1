package com.example.webapp.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.webapp.entity.VerificationConfig;
// import com.amazonaws.auth.AWSStaticCredentialsProvider;
// import com.amazonaws.auth.BasicAWSCredentials;
// import com.amazonaws.services.s3.AmazonS3;
// import com.amazonaws.services.s3.AmazonS3ClientBuilder;
// import com.amazonaws.services.s3.model.CannedAccessControlList;
// import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.webapp.exceptions.AddProductException;
import com.example.webapp.exceptions.CategoryNotFoundException;
import com.example.webapp.exceptions.FileConversionException;
import com.example.webapp.exceptions.FileUploadException;
import com.example.webapp.exceptions.InvalidCalculationException;
import com.example.webapp.exceptions.InvalidDateRangeException;
import com.example.webapp.exceptions.InvalidPaginationException;
import com.example.webapp.exceptions.InvalidPriceUpdateException;
import com.example.webapp.exceptions.KaratRateNotFoundException;
import com.example.webapp.exceptions.NoRatesAvailableException;
import com.example.webapp.exceptions.PermissionUpdateException;
import com.example.webapp.exceptions.ProductNotFoundException;
import com.example.webapp.exceptions.ProductRemovalException;
import com.example.webapp.exceptions.ProductUpdateException;
import com.example.webapp.exceptions.RateNotFoundException;
import com.example.webapp.exceptions.SessionHandlingException;
import com.example.webapp.exceptions.UserDeletionException;
import com.example.webapp.exceptions.UserNotFoundException;
import com.example.webapp.models.Category;
import com.example.webapp.models.Estimate;
import com.example.webapp.models.FrequencyRequest;
import com.example.webapp.models.LogRequest;
import com.example.webapp.models.Orders;
import com.example.webapp.models.Product;
import com.example.webapp.models.ProductPage;
import com.example.webapp.service.LogService;
import com.example.webapp.service.ProductService;
import com.example.webapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpSession;

import com.example.webapp.models.Rate;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.webapp.models.RateWrapper;
import com.example.webapp.models.UserRoleUpdate;
import com.example.webapp.models.UserTemp;
import com.example.webapp.models.VerificationUpdate;
import com.example.webapp.repository.VerificationConfigRepository;

@Controller
public class ProductController {

	@Autowired
	private UserService userService;

	@Autowired
	private ProductService productService;
	
	@Autowired
	private LogService logService;
	
	@Value("${app.server.host}")
    private String serverHost;

    @Value("${app.server.port}")
    private String serverPort;
    

    @Autowired
    private VerificationConfigRepository verificationConfigRepository;


//	@Autowired
//	private AmazonS3 s3Client;

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

//	private final String bucketName = "elasticbeanstalk-ap-south-1-012676044441"; // Replace with your bucket name
////	private final String region = "ap-south-1"; // Replace with your AWS region (e.g., "us-east-1")

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
	@GetMapping("/verify-product")
	public String verifyPoductPage() {
		return "verify_products"; // Return the verify_products.html view
	}

	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/get-estimate")
	public String getEstimatePage() {
		return "get_estimate"; // Return the view_product.html view
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/generate-report")
	public String getReportPage() {
		return "get_report"; // Return the view_product.html view
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/import-data")
	public String getImportDataPage() {
		return "import_data"; // Return the view_product.html view
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/permissions")
	public String permission() {
		return "permission"; // Renders the permission.html page
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/enquiry-log")
	public String enquiryLog() {
		return "enquiry_log"; // Renders the enquiry_log.html page
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/sales-log")
	public String salesLog() {
		return "sales_log"; // Renders the sales_log.html page
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/product-snapshot")
	public String productSnapshot() {
		return "product_snapshot"; // Renders the product_snapshot.html page
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/estimate-snapshot")
	public String estimateSnapshot() {
		return "estimate_snapshot"; // Renders the estimate_snapshot.html page
	}
	
	@PostMapping("/logEnquiry")
    public ResponseEntity<String> logEnquiry(@RequestBody LogRequest request) {
        logService.logEnquiry(request);
        return ResponseEntity.ok("Enquiry logged successfully");
    }

    @PostMapping("/logSale")
    public ResponseEntity<String> logSale(@RequestBody LogRequest request) {
        logService.logSale(request);
        return ResponseEntity.ok("Sale logged successfully and product freed");
    }

	@GetMapping("/categories")
	@ResponseBody
	public List<Map<String, Object>> getAllCategories() {
	    return productService.getAllCategories().stream()
	        .map(category -> {
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", category.getCategoryId());
	            map.put("name", category.getCategoryName());

	            if (category.getParentCategory() == null) {
	                map.put("isParent", true);
	                map.put("parentId", null);
	                map.put("parentName", null);
	            } else {
	                map.put("isParent", false);
	                map.put("parentId", category.getParentCategory().getCategoryId());
	                map.put("parentName", category.getParentCategory().getCategoryName());
	            }

	            return map;
	        })
	        .toList();
	}

	@GetMapping("/{categoryId}/name")
	public ResponseEntity<String> getCategoryNameById(@PathVariable("categoryId") Long categoryId) {
	    String categoryName = productService.getCategoryNameById(categoryId);
	    if (categoryName != null) {
	    	System.out.println("came here");
	        return ResponseEntity.ok(categoryName);
	    } else {
	    	System.out.println("came here else");
	        return ResponseEntity.notFound().build();
	    }
	}


	@PostMapping("/add")
	public ResponseEntity<Map<String, Object>> addProduct(@RequestParam("productName") String productName,
			@RequestParam(value = "price", required = false) BigDecimal price,
			@RequestParam("stockQuantity") Integer stockQuantity, @RequestParam("categoryId") Integer categoryId,
			@RequestParam("imageUrl") MultipartFile imageFile,
			@RequestParam(value = "orderId", required = false) String orderId,

			// Category-specific fields
			@RequestParam(value = "net", required = false) BigDecimal net,
			@RequestParam(value = "chainNet", required = false) BigDecimal chainNet,
			@RequestParam(value = "pcs", required = false) Integer pcs,
			@RequestParam(value = "diaWeight", required = false) BigDecimal diaWeight,
			@RequestParam(value = "diaRate", required = false) BigDecimal diaRate,
			@RequestParam(value = "diaSt", required = false) BigDecimal diaSt,
			@RequestParam(value = "diaStRate", required = false) BigDecimal diaStRate,
			@RequestParam(value = "remarks", required = false) String remarks,

			@RequestParam(value = "gross", required = false) BigDecimal gross,
			@RequestParam(value = "vilandiCt", required = false) BigDecimal vilandiCt,
			@RequestParam(value = "diamondsCt", required = false) BigDecimal diamondsCt,
			@RequestParam(value = "diamondsCtRate", required = false) BigDecimal diamondsCtRate,
			@RequestParam(value = "otherStonesCt", required = false) BigDecimal otherStonesCt,
			@RequestParam(value = "openStRate", required = false) BigDecimal openStRate,
			@RequestParam(value = "beadsCt", required = false) BigDecimal beadsCt,
			@RequestParam(value = "pearlsGm", required = false) BigDecimal pearlsGm,
			@RequestParam(value = "others", required = false) String others,
			@RequestParam(value = "designNoOS", required = false) String designNoOS,
			@RequestParam(value = "designNoDR", required = false) String designNoDR,
			@RequestParam(value = "designNo", required = false) String designNo,
			@RequestParam(value = "designNoEarring", required = false) String designNoEarring,
			@RequestParam(value = "designNoVilandi", required = false) String designNoVilandi,
			@RequestParam(value = "designNoJadtar", required = false) String designNoJadtar,
			@RequestParam(value = "earringNet", required = false) BigDecimal earringNet,
			@RequestParam(value = "earringPcs", required = false) Integer earringPcs,
			@RequestParam(value = "diamondWeightEarring", required = false) BigDecimal diamondWeightEarring,
			@RequestParam(value = "diamondsWtRate", required = false) BigDecimal diamondsWtRate,
			@RequestParam(value = "earSt", required = false) BigDecimal earSt,
			@RequestParam(value = "earStRate", required = false) BigDecimal earStRate,
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
			@RequestParam(value = "jadtarFitting", required = false) BigDecimal jadtarFittingRate,
			@RequestParam(value = "mozStone", required = false) BigDecimal mozStone,
			@RequestParam(value = "mozStoneRate", required = false) BigDecimal mozStoneRate,
			@RequestParam(value = "karatId", required = false) BigDecimal karat,
			@RequestParam(value = "drLabour", required = false) BigDecimal drLabour,
			@RequestParam(value = "osLabour", required = false) BigDecimal osLabour,
			@RequestParam(value = "chainLabour", required = false) BigDecimal chainLabour,
			@RequestParam(value = "diamondLabour", required = false) BigDecimal diamondLabour,
			@RequestParam(value = "vilandiLabour", required = false) BigDecimal vilandiLabour,
			@RequestParam(value = "jadtarLabour", required = false) BigDecimal jadtarLabour,
			@RequestParam(value = "drLabourAll", required = false) BigDecimal drLabourAll,
			@RequestParam(value = "osLabourAll", required = false) BigDecimal osLabourAll,
			@RequestParam(value = "chainLabourAll", required = false) BigDecimal chainLabourAll,
			@RequestParam(value = "diamondLabourAll", required = false) BigDecimal diamondLabourAll,
			@RequestParam(value = "vilandiLabourAll", required = false) BigDecimal vilandiLabourAll,
			@RequestParam(value = "jadtarLabourAll", required = false) BigDecimal jadtarLabourAll,
			@RequestParam(value = "categoryId", required = false) Long categoryIds,
			@RequestParam(value = "subCategoryId", required = false) Long subCategoryId,
			@RequestParam(value = "diamondGross", required = false) BigDecimal diamondGross,
			@RequestParam(value = "earringGross", required = false) BigDecimal earringGross,
			@RequestParam(value = "chainGross", required = false) BigDecimal chainGross,
			@RequestParam(value = "ssPearlCts", required = false) BigDecimal ssPearlCts,
			@RequestParam(value = "osSSPearlRate", required = false) BigDecimal osSSPearlRate,
			 @RequestParam(value = "vilandiGross", required = false) BigDecimal vilandiGross,
			 @RequestParam(value = "customFields", required = false) String customFieldsJson) {

		try {
			Product product = new Product();
			// Process the image file
			String imageUrl = saveImageFile(imageFile);
			System.out.println(imageUrl);
			// Create a Product entity from the parameters
			product.setItem(productName);
			product.setPrice(price);
			product.setStockQuantity(stockQuantity);
			product.setCategoryId(categoryId);
			product.setParentCategoryId(categoryIds);
			product.setSubCategoryId(subCategoryId);
			product.setImageUrl(imageUrl);// Set the image URL
			product.setRemarks(remarks);
			product.setKarat(karat);
			if (customFieldsJson != null && !customFieldsJson.isEmpty()) {
	            product.setCustomFields(customFieldsJson);
	        }
			/*
			 * System.out.println("design no dr "+designNoDR);
			 * System.out.println("design no er "+designNoEarring);
			 * System.out.println("sub category "+subCategoryId);
			 */
//			product.setOrderRef(existingOrderOpt.get());
			// Set category-specific fields based on categoryId
			if (categoryId == 1 && (subCategoryId != 19 && subCategoryId !=20)) { 
				System.out.println("inside diamond ring");// Diamond Rings
				product.setNet(net);
				product.setGross(diamondGross);
				product.setPcs(pcs);
				product.setDiamondsCt(diaWeight);
				product.setDiaRt(diaRate);
				product.setDesignNo(designNoDR);
				product.setLabour(diamondLabour);
				product.setLabourAll(diamondLabourAll);
				product.setOtherStonesCt(diaSt);
				product.setOtherStonesRt(diaStRate);
			} else if (categoryId == 2) { // Open Setting
				product.setGross(gross);
				product.setNet(net);
				product.setVilandiCt(vilandiCt);
				product.setvRate(vilandiRate);
				product.setDiamondsCt(diamondsCt);
				product.setDiaRt(diamondsCtRate);
				product.setBeadsCt(beadsCt);
				product.setBdRate(vilandiBeadsRate);
				product.setPearlsGm(pearlsGm);
				product.setPrlRate(vilandiPearlRate);
				product.setSsPearlCt(ssPearlCts);
				product.setSsRate(osSSPearlRate);
				product.setOtherStonesCt(otherStonesCt);
				product.setDesignNo(designNoOS);
				product.setLabour(osLabour);
				product.setLabourAll(osLabourAll);
				product.setOtherStonesCt(otherStonesCt);
				product.setOtherStonesRt(openStRate);
			} else if (categoryId == 3) { // Chains
				product.setDesignNo(designNo);
				product.setNet(chainNet);
				product.setGross(chainGross);
				product.setLabour(chainLabour);
				product.setLabourAll(chainLabourAll);
			} else if (subCategoryId == 19 || subCategoryId ==20) { // Diamond Earrings
			//	System.out.println("inside diamond Earrings");// Diamond Rings

				product.setDesignNo(designNoEarring);
				product.setNet(earringNet);
				product.setGross(earringGross);
				product.setPcs(earringPcs);
				product.setDiamondsCt(diamondWeightEarring);
				product.setDiaRt(diamondsWtRate);
				product.setLabour(diamondLabour);
				product.setLabourAll(diamondLabourAll);
				product.setOtherStonesCt(earSt);
				product.setOtherStonesRt(earStRate);
			} else if (categoryId == 4) { // Vilandi
				product.setDesignNo(designNoVilandi);
				product.setGross(vilandiGross);
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
				product.setLabour(vilandiLabour);
				product.setLabourAll(vilandiLabourAll);
			} else if (categoryId == 5) { // Jadtar Register
				System.out.println("Design is " + designNoJadtar);
				product.setDesignNo(designNoJadtar);
				product.setGross(jadtarGross);
				product.setNet(jadtarNet);
				product.setStones(jadtarStones);
				product.setStRate(jadtarStoneRate);
				product.setBeadsCt(jadtarBeads);
				product.setBdRate(jadtarBeadsRate);
				product.setPearlsGm(jadtarPearls);
				product.setPrlRate(jadtarPearlsRate);
				product.setSsPearlCt(jadtarSSPearl);
				product.setSsRate(jadtarSSPearlRate);
				product.setRealStone(jadtarRealStone);
				product.setFitting(jadtarFittingRate);
				product.setMozonite(mozStone);
				product.setmRate(mozStoneRate);
				product.setLabour(jadtarLabour);
				product.setLabourAll(jadtarLabourAll);
			}

			Orders order;
			Optional<Orders> existingOrderOpt = productService.findByOrderId(orderId);
			//System.out.println("order id is "+orderId);
			if (existingOrderOpt.isPresent()) {
			    // User selected from existing list
			    order = existingOrderOpt.get();
			    if (order.isAssigned()) {
			        throw new IllegalArgumentException("Order ID already assigned");
			    }
			    order.setAssigned(true);
			    order.setAssignedProduct(product);
			    product.setOrders(order);
			}
			 else {
				    // User gave a custom ID
				    order = new Orders();
			//	    System.out.println("new order id is "+orderId);
				    order.setOrderId(orderId);
				    order.setCategoryId(product.getCategoryId());
				    order.setAssigned(true);
				    order.setAssignedProduct(product);
				    product.setOrders(order);
				}
			
		//	String qrUrl = "http://" + serverHost + ":" + serverPort + "/loadProductByDesignNo/" + product.getDesignNo();
			String qrUrl = "https://say-comfort-statute-toilet.trycloudflare.com" + "/loadProductByDesignNo/" + product.getDesignNo();

			// 2. Define save location
			String qrDir = "C:/Users/Admin/Downloads/uploads/qr_codes/";
			Files.createDirectories(Paths.get(qrDir));
			String qrFileName = "QR_" + product.getDesignNo() + ".png";
			String qrFilePath = qrDir + qrFileName;

			// 3. Generate QR
			generateQRCodeImage(qrUrl, qrFilePath);
			
			String qr_path="/uploads/qr_codes/" + qrFileName;
			// 4. Save QR path in product
			product.setQrCodePath(qr_path);
			// Add the product using the service
			productService.addProduct(product);
			//	productService.saveOrders(order);
			Map<String, Object> response = new HashMap<>();
			response.put("message", "Product added successfully!");
			response.put("success", true);
			return ResponseEntity.ok(response);
		}catch (IllegalArgumentException e) {
		    // Let known service exceptions bubble up unchanged
		    throw e;
		} 
		
		catch (Exception e) {
			logger.error("Error occurred while adding product: {}", productName, e);
			throw new AddProductException("Error occurred while adding product: " + productName, e);
		}
	}
	
	@GetMapping("/loadProductByDesignNo/{designNo:[a-zA-Z0-9_-]+}")
	public String getProductByDesignNo(@PathVariable String designNo, Model model) {
	    Product product = productService.findProductById(designNo)
	            .orElseThrow(() -> new ProductNotFoundException("Product not found with design no: " + designNo));
	    System.out.println(product);
		RateWrapper rateWrapper = new RateWrapper();
		List<Rate> rates = productService.getAllRates();
		rateWrapper.resetRates();
		Iterator<Rate> it = rates.iterator();
		while (it.hasNext()) {
		    Rate rate = it.next();
		    String c = rate.getCommodity();
		    if (c == null) continue;

		    switch (c.toLowerCase(Locale.ROOT)) {
		        case "diamond": rateWrapper.diamondPrice = rate.getPrice(); break;
		        case "gst":     rateWrapper.gst = rate.getPrice(); break;
		        case "silver":  rateWrapper.silver = rate.getPrice(); break;
		        default: /* ignore */ 
		    }
		}
        int verificationFreqcy = getVerificationFrequency(); // get from DB
		Estimate e = new Estimate();
		List<BigDecimal> prices= calculateProductPrice(product, rateWrapper, e, rates);
		product.setPrice(prices.get(0));
		product.setPriceWithFields(prices.get(1));
		 boolean verified = isProductVerified(product, verificationFreqcy);
         if (verified) {
         	  product.setVerificationStatus(1);
         	} else {
         	  // preserve -1; set 0 only if not -1
         	  if (product.getVerificationStatus()!=-1) {
         	    product.setVerificationStatus(0);
         	  }
         	}
	    try {
	    	ObjectMapper mapper = new ObjectMapper();
	        mapper.registerModule(new JavaTimeModule());
	        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			model.addAttribute("productJson", mapper.writeValueAsString(product));
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    return "load_product"; 
	}

	
	// GET current frequency
    @GetMapping("/frequency")
    public ResponseEntity<Integer> getFrequency() {
        int days = productService.getVerificationDays();
        return ResponseEntity.ok(days);
    }

    // POST update frequency
    @PostMapping("/frequency")
    public ResponseEntity<String> setFrequency(@RequestBody FrequencyRequest request) {
    	productService.setVerificationDays(request.getFrequency());
        return ResponseEntity.ok("Verification frequency updated successfully");
    }
	
	@GetMapping("/available-order-ids")
	public ResponseEntity<List<String>> getAvailableOrderIds(@RequestParam(value = "categoryId", required = false) Long categoryId) {
	    List<Orders> orders = productService.findByCategoryIdAndIsAssignedFalse(categoryId);
	    List<String> availableOrderIds = orders.stream()
	                                           .map(Orders::getOrderId)
	                                           .collect(Collectors.toList());
	    return ResponseEntity.ok(availableOrderIds);
	}


	/*
	 * local image store 
	 */
	/*
	 * private String saveImageFile(MultipartFile imageFile) { try { // Specify the
	 * directory to save the images String uploadDir =
	 * "C:\\Users\\Admin\\Downloads\\uploads\\"; // Update to your desired upload
	 * directory Files.createDirectories(Paths.get(uploadDir));
	 * 
	 * // Define the path where the file will be saved Path filePath =
	 * Paths.get(uploadDir + imageFile.getOriginalFilename());
	 * 
	 * // Save the file imageFile.transferTo(filePath.toFile());
	 * 
	 * // Construct a URL to access the image String imageUrl =
	 * "http://localhost:8081/" + imageFile.getOriginalFilename(); // Adjust based
	 * on your context return imageUrl; } catch (IOException e) {
	 * e.printStackTrace(); return null; } }
	 */
	
	@PostMapping("/upload-image")
	public String saveImageFile(@RequestParam("imageFile") MultipartFile imageFile) {
	    try {
	        // Save directory
	        String uploadDir = "C:/Users/Admin/Downloads/uploads/";
	        Files.createDirectories(Paths.get(uploadDir));

	        // Get file extension
	        String originalFilename = imageFile.getOriginalFilename();
	        String extension = "";
	        if (originalFilename != null && originalFilename.contains(".")) {
	            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
	        }

	        // Generate 8-digit random number
	        String newFileName = "IMG_" + String.format("%08d", (int)(Math.random() * 100_000_000)) + extension;

	        // Save file
	        Path filePath = Paths.get(uploadDir, newFileName);
	        imageFile.transferTo(filePath.toFile());

	        // Return path for frontend
	        return "/uploads/" + newFileName;

	    } catch (IOException e) {
	        e.printStackTrace();
	        return "Upload failed!";
	    }
	}

	 
	public static String generateQRCodeImage(String text, String filePath) 
	        throws WriterException, IOException {

	    QRCodeWriter qrCodeWriter = new QRCodeWriter();

	    Map<EncodeHintType, Object> hints = new HashMap<>();
	    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
	    hints.put(EncodeHintType.MARGIN, 1);

	    // Generate higher resolution (300x300 px)
	    BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300, hints);

	    Path path = FileSystems.getDefault().getPath(filePath);
	    MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

	    return filePath;
	}


	// public String saveImageFile(MultipartFile file) {
	// 	try {
	// 		File fileObj = convertMultiPartFileToFile(file);
	// 		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
	// 		s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj)
	// 				.withCannedAcl(CannedAccessControlList.PublicRead));
	// 		fileObj.delete();
	// 		return s3Client.getUrl(bucketName, fileName).toString();
	// 	} catch (Exception e) {
	// 		throw new FileUploadException("Error uploading file: " + file.getOriginalFilename(), e);
	// 	}
	// }

	// private File convertMultiPartFileToFile(MultipartFile file) {
	// 	File convertedFile = new File(file.getOriginalFilename());
	// 	try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
	// 		fos.write(file.getBytes());
	// 	} catch (IOException e) {
	// 		throw new FileConversionException("Error converting multipartFile to file: " + file.getOriginalFilename(),
	// 				e);
	// 	}
	// 	return convertedFile;
	// }

	@PostMapping("/remove/{productId}")
	public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable String productId) {
		try {
			productService.removeProduct(productId);
			Map<String, Object> response = new HashMap<>();
			response.put("message", "Product deleted successfully!");
			response.put("success", true);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			throw new ProductRemovalException("Failed to remove product with ID: " + productId, e);
		}
	}

	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Long categoryId) {
		List<Product> products = productService.findProductsByCategoryId(categoryId);
		if (products.isEmpty()) {
			throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
		}
		return ResponseEntity.ok(products);
	}

	@GetMapping("loadProduct/{id}")
	public ResponseEntity<Product> getProduct(@PathVariable String id) {
		Product product = productService.findProductById(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
		return ResponseEntity.ok(product);
	}

	@GetMapping("loadProductDetail/{id}")
	public ResponseEntity<Product> getProduct(@PathVariable Long id) {
		Product product = productService.findProductFromId(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
		RateWrapper rateWrapper = new RateWrapper();
		List<Rate> rates = productService.getAllRates();
		rateWrapper.resetRates();
		Iterator<Rate> it = rates.iterator();
		while (it.hasNext()) {
		    Rate rate = it.next();
		    String c = rate.getCommodity();
		    if (c == null) continue;

		    switch (c.toLowerCase(Locale.ROOT)) {
		        case "diamond": rateWrapper.diamondPrice = rate.getPrice(); break;
		        case "gst":     rateWrapper.gst = rate.getPrice(); break;
		        case "silver":  rateWrapper.silver = rate.getPrice(); break;
		        default: /* ignore */ 
		    }
		}

		Estimate e = new Estimate();
		List<BigDecimal> prices= calculateProductPrice(product, rateWrapper, e, rates);
		System.out.println("prices "+prices);
		product.setPrice(prices.get(0));
		product.setPriceWithFields(prices.get(1));
		System.out.println(product);
		return ResponseEntity.ok(product);
	}

	 @GetMapping("/product/load")
	    public ResponseEntity<ProductPage> loadProducts(
	            @RequestParam int page,
	            @RequestParam int size,
	            @RequestParam(required = false) String category,
	            @RequestParam(required = false, defaultValue = "") String searchTerm,
	            @RequestParam(required = false, defaultValue = "name") String searchBy, // <-- new
	            @RequestParam(required = false) String sortBy,
	            HttpSession session) {

	        if (page < 0 || size <= 0) {
	            throw new InvalidPaginationException("Page index must be 0 or greater, and size must be greater than 0.");
	        }

	        // Parse category ids (comma-separated) -> List<Integer>
	        List<Integer> categories = null;
	        if (category != null && !category.isBlank()) {
	            categories = Arrays.stream(category.split(","))
	                    .map(String::trim)
	                    .map(Integer::valueOf)
	                    .toList();
	        }

	        // Reset pagination when filters change (searchTerm/category/searchBy)
	        try {
	            String prevSearchTerm = (String) session.getAttribute("prevSearchTerm");
	            @SuppressWarnings("unchecked")
	            List<Integer> prevCategory = (List<Integer>) session.getAttribute("prevCategory");
	            String prevSearchBy = (String) session.getAttribute("prevSearchBy");

	            boolean filtersChanged = false;
	            if (!Objects.equals(searchTerm, session.getAttribute("prevSearchTerm"))) {
	                filtersChanged = true;
	            }
	            if (!Objects.equals(categories, session.getAttribute("prevCategory"))) {
	                filtersChanged = true;
	            }
	            if (!Objects.equals(searchBy, session.getAttribute("prevSearchBy"))) {
	                filtersChanged = true;
	            }

	            if (filtersChanged && page == 0) {
	                // Only reset page if it's first page (optional)
	                page = 0;
	            }

	            session.setAttribute("prevSearchTerm", searchTerm);
	            session.setAttribute("prevCategory", categories);
	            session.setAttribute("prevSearchBy", searchBy);
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new SessionHandlingException("Error accessing session attributes.");
	        }

	        // Build Sort from sortBy flag
	        Sort sort = Sort.unsorted();
	        if ("old".equalsIgnoreCase(sortBy)) {
	            sort = Sort.by(Sort.Order.asc("createDateTime"));
	        } else if ("recent".equalsIgnoreCase(sortBy)) {
	            sort = Sort.by(Sort.Order.desc("createDateTime"));
	        } else if ("nameAsc".equalsIgnoreCase(sortBy)) {
	            sort = Sort.by(Sort.Order.asc("item"));
	        } else if ("nameDesc".equalsIgnoreCase(sortBy)) {
	            sort = Sort.by(Sort.Order.desc("item"));
	        } else if ("none".equalsIgnoreCase(sortBy) || sortBy == null || sortBy.isEmpty()) {
	            sort = Sort.unsorted();
	        }
	        PageRequest pageable = PageRequest.of(page, size, sort);

	        System.out.println("cateogries "+categories);
	        // Execute search based on presence of category filter and searchBy
	        Page<Product> products;
	        System.out.println("Pageable: " + pageable);

	        if (categories == null) {
	            products = productService.searchWithoutCategory(searchTerm, searchBy, pageable);
	        } else {
	            products = productService.searchByCategory(categories, searchTerm, searchBy, pageable);
	        }

	        if (products.isEmpty()) {
	            throw new ProductNotFoundException("No products found for the given filters.");
	        }

	        // Rates/price calculation (unchanged)
	        List<Rate> rates = productService.getAllRates();
	        RateWrapper rateWrapper = new RateWrapper();
	        for (Rate rate : rates) {
	            switch (rate.getCommodity().toLowerCase()) {
	                case "diamond" -> rateWrapper.diamondPrice = rate.getPrice();
	                case "gst" -> rateWrapper.gst = rate.getPrice();
	                case "silver" -> rateWrapper.silver = rate.getPrice();
	            }
	        }

	        Estimate e = new Estimate();
	        
	        int verificationFreqcy = getVerificationFrequency(); // get from DB
	        LocalDateTime now = LocalDateTime.now();

	        products.forEach(product -> {
	            List<BigDecimal> prices = calculateProductPrice(product, rateWrapper, e, rates);
	            product.setPrice(prices.get(0));
	            product.setPriceWithFields(prices.get(1));
	            // Verification logic
	            boolean verified = isProductVerified(product, verificationFreqcy);
	            if (verified) {
	            	  product.setVerificationStatus(1);
	            	} else {
	            	  // preserve -1; set 0 only if not -1
	            	  if (product.getVerificationStatus()!=-1) {
	            	    product.setVerificationStatus(0);
	            	  }
	            	}
	        });

	        return ResponseEntity.ok(new ProductPage(products.getContent(), products.getTotalElements()));
	    }

	 public static boolean isProductVerified(Product product, int verificationFrequencyDays) {
	        if (product == null) return false;
	        LocalDateTime verificationDate = product.getVerificationDate();
	        if (verificationDate == null) return false;
	        if (product.getVerificationStatus() != 1) return false;

	        LocalDateTime now = LocalDateTime.now();
	        long daysSinceVerification = ChronoUnit.DAYS.between(verificationDate, now);
	        return daysSinceVerification <= verificationFrequencyDays;
	    }
	 
	 public int getVerificationFrequency() {
	        return verificationConfigRepository.findAll()
	                .stream()
	                .findFirst()
	                .map(VerificationConfig::getVerificationDays)
	                .orElse(1); // default 1 day
	    }

	 
	private List<BigDecimal> calculateProductPrice(Product product, RateWrapper rateWrapper, Estimate e, List<Rate> r) {
		try {
			BigDecimal labour = null;
			BigDecimal gold1 = null;
			BigDecimal estimate_gst = null;
			BigDecimal estimate_nogst = null;
			BigDecimal estimate_nogst_withfields = null;
			BigDecimal estimate_gst_withfields = null;
			BigDecimal total = null;
			BigDecimal total_withfields = null;
			BigDecimal additionalFieldsTotal = BigDecimal.ZERO;
	        String customFieldsJson = product.getCustomFields();
	        if (customFieldsJson != null && !customFieldsJson.isEmpty() && !customFieldsJson.equals("{}")) {
	            try {
	                // Parse JSON string
	                ObjectMapper mapper = new ObjectMapper();
	                Map<String, Map<String, String>> extras = mapper.readValue(customFieldsJson, new TypeReference<Map<String, Map<String, String>>>(){});

	                for (Map<String, String> value : extras.values()) {
	                    BigDecimal qty = new BigDecimal(value.getOrDefault("qty", "0"));
	                    BigDecimal rate = new BigDecimal(value.getOrDefault("rate", "0"));
	                    additionalFieldsTotal = additionalFieldsTotal.add(qty.multiply(rate));
	                }
	            } catch (Exception ex) {
	                // Log parsing error, treat additionalFieldsTotal as zero
	                System.err.println("Error parsing additional fields in estimate: " + ex.getMessage());
	            }
	        }

			if (nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(24.0000)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(22.0000)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(18.0000)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(14.0000)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(10.0000)) == 0) {

				Rate rate = getKaratRate(product, r);
				if (rate == null) {
					throw new RateNotFoundException("No rate found for karat: " + product.getKarat());
				}
				rateWrapper.goldPrice = rate.getPrice();
			} else {
				rateWrapper.goldPrice = BigDecimal.ZERO;

			}

			if (product.getLabour() != null && product.getLabour().compareTo(BigDecimal.ZERO) != 0) {
				labour = nullSafe(product.getNet()).multiply(nullSafe(product.getLabour()));
			} else {
				labour = nullSafe(product.getLabourAll());
			}
			e.setLabour(labour);

			gold1 = nullSafe(rateWrapper.goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP));

			e.setGold(nullSafe(product.getNet()).multiply(gold1));
			e.setStones(nullSafe(product.getStones()).multiply(nullSafe(product.getStRate())));
			e.setBeads(nullSafe(product.getBeadsCt()).multiply(nullSafe(product.getBdRate())));
			e.setPearls(nullSafe(product.getPearlsGm()).multiply(nullSafe(product.getPrlRate())));
			e.setSsPearls(nullSafe(product.getSsPearlCt()).multiply(nullSafe(product.getSsRate())));
			e.setVilandi(nullSafe(product.getVilandiCt()).multiply(nullSafe(product.getvRate())));
			e.setMozo(nullSafe(product.getMozonite()).multiply(nullSafe(product.getmRate())));
			e.setOtherStones(nullSafe(product.getOtherStonesCt()).multiply(nullSafe(product.getOtherStonesRt())));
			e.setDiamonds(nullSafe(product.getDiamondsCt()).multiply(nullSafe(product.getDiaRt())));
			e.setRealStones(nullSafe(product.getRealStone()));
			e.setFitting(nullSafe(product.getFitting()));

			estimate_nogst = nullSafe(e.getGold().add(e.getLabour()).add(e.getStones()).add(e.getBeads())
					.add(e.getPearls()).add(e.getSsPearls()).add(e.getVilandi()).add(e.getMozo()).add(e.getRealStones())
					.add(e.getFitting()).add(e.getDiamonds()).add(e.getOtherStones()));
			e.setNogsttotal(estimate_nogst);
			estimate_nogst_withfields = nullSafe(e.getGold().add(e.getLabour()).add(e.getStones()).add(e.getBeads())
					.add(e.getPearls()).add(e.getSsPearls()).add(e.getVilandi()).add(e.getMozo()).add(e.getRealStones())
					.add(e.getFitting()).add(e.getDiamonds()).add(e.getOtherStones()).add(additionalFieldsTotal));
			e.setEstimate_nogst_withfields(estimate_nogst_withfields);
			estimate_gst = nullSafe(
					estimate_nogst.multiply(rateWrapper.gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP));
			estimate_gst_withfields = nullSafe(
					estimate_nogst_withfields.multiply(rateWrapper.gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP));
			e.setEstimate_gst_withfields(estimate_gst_withfields);
			e.setGst(estimate_gst);
			
			

			total = nullSafe(estimate_nogst.add(estimate_gst));
			total_withfields = nullSafe(estimate_nogst_withfields.add(estimate_gst_withfields));
			e.setTotal(total);
			e.setTotalWithAdditionalFields(total_withfields);
			List<BigDecimal> prices =  new ArrayList<>();
			prices.add(total != null ? total : BigDecimal.ZERO);
			prices.add(total_withfields != null ? total_withfields : BigDecimal.ZERO);
			return prices;
		} catch (ArithmeticException ex) {
			throw new InvalidCalculationException("Error occurred while calculating product price", ex);
		}
	}

	@PutMapping(value = "/updateProduct/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<Product> updateProduct(@PathVariable String id,
			@RequestParam(value = "productName", required = false) String productName,
			@RequestParam(value = "productOrderid", required = false) String productOrderid,
			@RequestParam(value = "productPrice", required = false) BigDecimal productPrice,
			@RequestParam(value = "productRemarks", required = false) String productRemarks,
			@RequestParam(value = "productImageUrl", required = false) String productImageUrl,
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
			@RequestParam(value = "subCategoryId", required = false) Long subCategoryId,
			@RequestParam(value = "productNet", required = false) BigDecimal productNet,
			@RequestParam(value = "pcs", required = false) Integer pcs,
			@RequestParam(value = "diaWeight", required = false) BigDecimal diaWeight,
			@RequestParam(value = "diaRate", required = false) BigDecimal diaRate,
			@RequestParam(value = "diaOs", required = false) BigDecimal diaOs,
			@RequestParam(value = "diaOsRate", required = false) BigDecimal diaOsRate,
			@RequestParam(value = "gross", required = false) BigDecimal gross,

			// Category-specific fields
			@RequestParam(value = "vilandiCt", required = false) BigDecimal vilandiCt,
			@RequestParam(value = "diamondsCt", required = false) BigDecimal diamondsCt,
			@RequestParam(value = "diamondsCtRate", required = false) BigDecimal diamondsCtRate,
			@RequestParam(value = "beadsCt", required = false) BigDecimal beadsCt,
			@RequestParam(value = "pearlsGm", required = false) BigDecimal pearlsGm,
			@RequestParam(value = "vilandiRate", required = false) BigDecimal vilandiRate,
			@RequestParam(value = "beadsRate", required = false) BigDecimal beadsRate,
			@RequestParam(value = "openPearlsRate", required = false) BigDecimal openPearlsRate,
			@RequestParam(value = "otherStonesCt", required = false) BigDecimal otherStonesCt,
			@RequestParam(value = "otherOsRate", required = false) BigDecimal otherOsRate,
			@RequestParam(value = "others", required = false) String others,
			@RequestParam(value = "designNo", required = false) String designNo,
			@RequestParam(value = "designNoEarrings", required = false) String designNoEarrings,
			@RequestParam(value = "pcsEarrings", required = false) Integer pcsEarrings,
			@RequestParam(value = "diamondsCtEarrings", required = false) BigDecimal diamondsCtEarrings,
			@RequestParam(value = "diamondsCtEarringsRate", required = false) BigDecimal diamondsCtEarringsRate,
			@RequestParam(value = "diaEOs", required = false) BigDecimal diaEOs,
			@RequestParam(value = "diaEOsRate", required = false) BigDecimal diaEOsRate,
			@RequestParam(value = "designNoVilandi", required = false) String designNoVilandi,
			@RequestParam(value = "designNoOS", required = false) String designNoOS,
			@RequestParam(value = "designNoDR", required = false) String designNoDR,
			@RequestParam(value = "grossVilandi", required = false) BigDecimal grossVilandi,

			@RequestParam(value = "vilandi", required = false) BigDecimal vilandi,
			@RequestParam(value = "vRate", required = false) BigDecimal vRate,
			@RequestParam(value = "stones", required = false) BigDecimal stones,
			@RequestParam(value = "vsRate", required = false) BigDecimal vsRate,
			@RequestParam(value = "beadsCtVilandi", required = false) BigDecimal beadsCtVilandi,
			@RequestParam(value = "vbRate", required = false) BigDecimal vbRate,
			@RequestParam(value = "pearlsGmVilandi", required = false) BigDecimal pearlsGmVilandi,
			@RequestParam(value = "vpRate", required = false) BigDecimal vpRate,
			@RequestParam(value = "ssPearlCt", required = false) BigDecimal ssPearlCt,
			@RequestParam(value = "vssRate", required = false) BigDecimal vssRate,
			@RequestParam(value = "vrealStone", required = false) BigDecimal vrealStone,
			@RequestParam(value = "vfitting", required = false) BigDecimal vfitting,
			@RequestParam(value = "vmoz", required = false) BigDecimal vmoz,
			@RequestParam(value = "vmRate", required = false) BigDecimal vmRate,
			@RequestParam(value = "designNoJadtar", required = false) String designNoJadtar,
			@RequestParam(value = "grossJadtar", required = false) BigDecimal grossJadtar,
			@RequestParam(value = "stonesJadtar", required = false) BigDecimal stonesJadtar,
			@RequestParam(value = "jsRate", required = false) BigDecimal jsRate,
			@RequestParam(value = "beadsCtJadtar", required = false) BigDecimal beadsCtJadtar,
			@RequestParam(value = "jbRate", required = false) BigDecimal jbRate,
			@RequestParam(value = "pearlsGmJadtar", required = false) BigDecimal pearlsGmJadtar,
			@RequestParam(value = "jpRate", required = false) BigDecimal jpRate,
			@RequestParam(value = "ssPearlCtJadtar", required = false) BigDecimal ssPearlCtJadtar,
			@RequestParam(value = "jssRate", required = false) BigDecimal jssRate,
			@RequestParam(value = "realStoneJadtar", required = false) BigDecimal realStoneJadtar,
			@RequestParam(value = "jfitting", required = false) BigDecimal jfitting,
			@RequestParam(value = "jmoz", required = false) BigDecimal jmoz,
			@RequestParam(value = "jmRate", required = false) BigDecimal jmRate,
			@RequestParam("image") MultipartFile imageFile,
			@RequestParam(value = "karat", required = false) BigDecimal karat,
			@RequestParam(value = "labour", required = false) BigDecimal labour,
			@RequestParam(value = "labourAll", required = false) BigDecimal labourAll,
			@RequestParam(value = "ssosPearlCt", required = false) BigDecimal ssosPearlCt,
			@RequestParam(value = "ssosPearllbl", required = false) BigDecimal ssosPearllbl,
			@RequestParam(value = "customFields", required = false) String customFieldsJson,
			@RequestParam(value = "grossPg", required = false) BigDecimal grossPg,
			@RequestParam(value = "grossDia", required = false) BigDecimal grossDia
			) {

		if (id == null || id.isEmpty()) {
			throw new ProductUpdateException("Product ID cannot be null or empty.");
		}

		if (categoryId == null) {
			throw new ProductUpdateException("Category ID is required.");
		}
		System.out.println("order id is "+productOrderid);
		//System.out.println("existing order id is "+existingOrderIds);
		Product updatedProduct = new Product();
		 if(customFieldsJson != null) {
			 updatedProduct.setCustomFields(customFieldsJson);
		    }
		// String design ="";
		if (categoryId == 1) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setSubCategoryId(subCategoryId);
			updatedProduct.setNet(productNet);
			updatedProduct.setGross(grossDia);
			updatedProduct.setPcs(pcs);
			updatedProduct.setDesignNo(designNoDR);
			updatedProduct.setDiamondsCt(diaWeight);
			updatedProduct.setOtherStonesCt(diaOs);
			updatedProduct.setOtherStonesRt(diaOsRate);
			updatedProduct.setDiaRt(diaRate);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			// design=designNo;
		}
		if (categoryId == 2) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setSubCategoryId(subCategoryId);
			updatedProduct.setNet(productNet);
			updatedProduct.setGross(gross);
			updatedProduct.setVilandiCt(vilandiCt);
			updatedProduct.setvRate(vilandiRate);
			updatedProduct.setDiamondsCt(diamondsCt);
			updatedProduct.setDiaRt(diamondsCtRate);
			updatedProduct.setOtherStonesCt(otherStonesCt);
			updatedProduct.setOtherStonesRt(otherOsRate);
			updatedProduct.setBeadsCt(beadsCt);
			updatedProduct.setBdRate(beadsRate);
			updatedProduct.setPearlsGm(pearlsGm);
			updatedProduct.setPrlRate(openPearlsRate);
			// updatedProduct.setStones(otherStonesCt);
			updatedProduct.setOthers(others);
			updatedProduct.setDesignNo(designNoOS);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			updatedProduct.setSsPearlCt(ssosPearllbl);
			updatedProduct.setSsRate(ssosPearlCt);
			// design=designNo;
		}
		if (categoryId == 3) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setSubCategoryId(subCategoryId);
			updatedProduct.setDesignNo(designNo);
			updatedProduct.setNet(productNet);
			updatedProduct.setGross(grossPg);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			// design=designNo;
		}
//		if (categoryId == 40) {
//		
//			updatedProduct.setItem(productName);
//			updatedProduct.setPrice(productPrice);
//			updatedProduct.setRemarks(productRemarks);
//			updatedProduct.setImageUrl(productImageUrl);
//			updatedProduct.setCategoryId(categoryId);
//			updatedProduct.setDesignNo(designNoEarrings);
//			updatedProduct.setNet(productNet);
//			updatedProduct.setPcs(pcsEarrings);
//			updatedProduct.setDiamondsCt(diamondsCtEarrings);
//			updatedProduct.setDiaRt(diamondsCtEarringsRate);
//			updatedProduct.setOtherStonesCt(diaEOs);
//			updatedProduct.setOtherStonesRt(diaEOsRate);
//			updatedProduct.setLabour(labour);
//			updatedProduct.setLabourAll(labourAll);
//			updatedProduct.setKarat(karat);
//			// design=designNoEarrings;
//		}
		if (categoryId == 4) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setSubCategoryId(subCategoryId);
			updatedProduct.setNet(productNet);
			updatedProduct.setDesignNo(designNoVilandi);
			updatedProduct.setGross(grossVilandi);
			updatedProduct.setVilandiCt(vilandi);
			updatedProduct.setvRate(vRate);
			updatedProduct.setStones(stones);
			updatedProduct.setStRate(vsRate);
			updatedProduct.setBeadsCt(beadsCtVilandi);
			updatedProduct.setBdRate(vbRate);
			updatedProduct.setPearlsGm(pearlsGmVilandi);
			updatedProduct.setPrlRate(vpRate);
			updatedProduct.setSsPearlCt(ssPearlCt);
			updatedProduct.setSsRate(vssRate);
			updatedProduct.setRealStone(vrealStone);
			updatedProduct.setFitting(vfitting);
			updatedProduct.setMozonite(vmoz);
			updatedProduct.setmRate(vmRate);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			// design=designNoVilandi;
		}
		if (categoryId == 5) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setSubCategoryId(subCategoryId);
			updatedProduct.setNet(productNet);
			updatedProduct.setDesignNo(designNoJadtar);
			updatedProduct.setGross(grossJadtar);
			updatedProduct.setStones(stonesJadtar);
			updatedProduct.setStRate(jsRate);
			updatedProduct.setBeadsCt(beadsCtJadtar);
			updatedProduct.setBdRate(jbRate);
			updatedProduct.setPearlsGm(pearlsGmJadtar);
			updatedProduct.setPrlRate(jpRate);
			updatedProduct.setSsPearlCt(ssPearlCtJadtar);
			updatedProduct.setSsRate(jssRate);
			updatedProduct.setRealStone(realStoneJadtar);
			updatedProduct.setFitting(jfitting);
			updatedProduct.setMozonite(jmoz);
			updatedProduct.setmRate(jmRate);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			// design=designNoJadtar;
		}

		Product updated = productService.updateProduct(id, updatedProduct, imageFile,productOrderid);

		if (updated == null) {
			throw new ProductUpdateException("Failed to update product. Product not found with ID: " + id);
		}
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/getEstimate/{productId}")
	public ResponseEntity<Map<String, Object>> getEstimate(@PathVariable Long productId) {
		Product product = productService.findProductFromId(productId)
				.orElseThrow(() -> new ProductNotFoundException("product not not found for id " + productId));
		RateWrapper rateWrapper = new RateWrapper();
		List<Rate> rates = productService.getAllRates();
		rateWrapper.resetRates();
		for (Rate rate : rates) {
			switch (rate.getCommodity().toLowerCase()) {
			/*
			 * case "gold": rateWrapper.goldPrice = rate.getPrice(); break;
			 */
			case "diamond":
				rateWrapper.diamondPrice = rate.getPrice();
				break;
			case "gst":
				rateWrapper.gst = rate.getPrice();
				break;
			case "silver":
				rateWrapper.silver = rate.getPrice();
				break;
			}
		}
		Estimate e = new Estimate();
		List<BigDecimal> prices= calculateProductPrice(product, rateWrapper, e, rates);
		product.setPrice(prices.get(0));
		product.setPriceWithFields(prices.get(1));
	//	System.out.println("pricec with additional fields "+product.getPriceWithFields());
		Map<String, Object> response = new HashMap<>();
		response.put("object1", product);
		response.put("object2", e);
		response.put("object3", rates);
		return ResponseEntity.ok(response);

	}

	private Rate getKaratRate(Product product, List<Rate> ratesList) {
		BigDecimal productKarat = nullSafe(product.getKarat());

		for (Rate rate : ratesList) {
			if (rate.getCommodity().equals(productKarat.toString())) {
				return rate; // Return the matched Rates object
			}
		}

		throw new KaratRateNotFoundException(productKarat);
	}

	@GetMapping("/prices")
	public ResponseEntity<List<Rate>> getAllRates() {
		List<Rate> rates = productService.getAllRates();
		if (rates.isEmpty()) {
			throw new NoRatesAvailableException("No rates available at the moment.");
		}
		return ResponseEntity.ok(rates);
	}

	@PostMapping("/updatePrices")
	public ResponseEntity<String> updatePrices(@RequestBody Map<String, BigDecimal> prices) {
		if (prices == null || prices.isEmpty()) {
			throw new InvalidPriceUpdateException("Price update request is empty or invalid.");
		}
		String response = productService.updatePrices(prices);

		if (response == null || response.isBlank()) {
			throw new InvalidPriceUpdateException("Failed to update prices. Please try again.");
		}
		return ResponseEntity.ok(response);
	}

	@GetMapping("/rates")
	@ResponseBody
	public List<Rate> getRates() {
		List<Rate> rates = productService.findAll();
		if (rates.isEmpty()) {
			throw new NoRatesAvailableException("No rates available at the moment.");
		}
		return rates;
	}

	@GetMapping("/getReport/{categoryId}")
	public ResponseEntity<Page<Product>> getProductsByCategory(@PathVariable("categoryId") Long categoryId,
			@RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
			@RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		if (startDate.isAfter(endDate)) {
			throw new InvalidDateRangeException("Start date cannot be after end date.");
		}
		ZonedDateTime startDateTime = startDate.atZone(ZoneId.of("Asia/Kolkata"));
		ZonedDateTime endDateTime = endDate.atZone(ZoneId.of("Asia/Kolkata"));
		startDate = startDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime();
		endDate = endDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime();

		Page<Product> products = productService.getFilteredProducts(categoryId, startDate, endDate, page, size);
		if (products.isEmpty()) {
			throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
		}
		List<Rate> rates = productService.getAllRates();
		RateWrapper rateWrapper = new RateWrapper();
		// Fetch rates for calculations

		for (Rate rate : rates) {
			switch (rate.getCommodity().toLowerCase()) {
			/*
			 * case "gold": rateWrapper.goldPrice = rate.getPrice(); break;
			 */
			case "diamond":
				rateWrapper.diamondPrice = rate.getPrice();
				break;
			case "gst":
				rateWrapper.gst = rate.getPrice();
				break;
			case "silver":
				rateWrapper.silver = rate.getPrice();
				break;
			}
		}
		Estimate e = new Estimate();
		products.forEach(product -> {
			List<BigDecimal> prices= calculateProductPrice(product, rateWrapper, e, rates);
			product.setPrice(prices.get(0));
			product.setPriceWithFields(prices.get(1));
		});
		return products.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}
	
	@GetMapping("/getReportAll/{categoryId}")
	public ResponseEntity<List<Product>> getAllProductsByCategory(
	        @PathVariable("categoryId") Long categoryId) {

	    List<Product> allProducts = productService.getAllProductsByCategory(categoryId);

	    if (allProducts.isEmpty()) {
	        throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
	    }

	    List<Rate> rates = productService.getAllRates();
	    RateWrapper rateWrapper = new RateWrapper();
	    for (Rate rate : rates) {
	        switch (rate.getCommodity().toLowerCase()) {
			/*
			 * case "gold": rateWrapper.goldPrice = rate.getPrice(); break;
			 */
	            case "diamond":
	                rateWrapper.diamondPrice = rate.getPrice();
	                break;
	            case "gst":
	                rateWrapper.gst = rate.getPrice();
	                break;
	            case "silver":
	                rateWrapper.silver = rate.getPrice();
	                break;
	        }
	    }

	    Estimate e = new Estimate();
	    allProducts.forEach(product -> {
	    	List<BigDecimal> prices= calculateProductPrice(product, rateWrapper, e, rates);
			product.setPrice(prices.get(0));
			product.setPriceWithFields(prices.get(1));
	    });

	    return ResponseEntity.ok(allProducts);
	}

	 @PutMapping("/verify/{designNo:[a-zA-Z0-9_-]+}")
	    public ResponseEntity<Void> setVerification(
	            @PathVariable String designNo,
	            @RequestBody VerificationUpdate body) {
		 System.out.println("came here");
	        int status = body.verificationStatus() != null ? body.verificationStatus() : 1;
	        productService.updateVerificationByDesignNo(designNo, status);
	        return ResponseEntity.noContent().build();
	    }

	@PostMapping("/uploadFile")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("category") String category) {
		try {
			productService.importProductsFromExcel(file, category);
			return ResponseEntity.ok("Data imported successfully");
		} catch (Exception e) {
			throw new FileUploadException("Failed to import data: " + e.getMessage(), e);
		}
	}

	private BigDecimal nullSafe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	@GetMapping("/all")
	public ResponseEntity<List<UserTemp>> getAllUsers() {
		List<UserTemp> users = userService.getAllUsers();
		if (users.isEmpty()) {
			throw new UserNotFoundException("No users found in the system.");
		}
		return ResponseEntity.ok(users);
	}

	@PostMapping("/update-permissions")
	public ResponseEntity<String> updatePermissions(@RequestBody List<UserRoleUpdate> updates) {
		try {
			userService.updatePermissions(updates);
			return ResponseEntity.ok("Permissions updated successfully!");
		} catch (Exception e) {
			throw new PermissionUpdateException("Failed to update permissions: " + e.getMessage(), e);
		}
	}

	@DeleteMapping("/removeUser/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
		try {
			userService.deleteUser(id);
			return ResponseEntity.ok("User deleted successfully.");
		} catch (Exception e) {
			throw new UserDeletionException("Error deleting user: " + e.getMessage(), e);
		}
	}
	
	@GetMapping("/proxy-image")
	public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
	    try (InputStream in = new URL(url).openStream()) {
	        byte[] imageBytes = in.readAllBytes();
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.IMAGE_JPEG);
	        headers.add("Access-Control-Allow-Origin", "*");
	        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	    }
	}

}

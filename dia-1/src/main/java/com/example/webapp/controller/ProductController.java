package com.example.webapp.controller;

import java.io.ByteArrayOutputStream;
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
import java.time.LocalDate;
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
import org.apache.pdfbox.util.Matrix;
import java.text.DecimalFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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
import com.example.webapp.models.BatchUpdateRequest;
import com.example.webapp.models.Category;
import com.example.webapp.models.CustomTagRequest;
import com.example.webapp.models.Estimate;
import com.example.webapp.models.FrequencyRequest;
import com.example.webapp.models.LogRequest;
import com.example.webapp.models.Orders;
import com.example.webapp.models.Product;
import com.example.webapp.models.ProductPage;
import com.example.webapp.models.RateHistory;
import com.example.webapp.service.LogService;
import com.example.webapp.service.ProductService;
import com.example.webapp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
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
import org.springframework.web.server.ResponseStatusException;

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
    
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.qr-dir}")
    private String qrDir;

    @Value("${app.qr-public-path}")
    private String qrPublicPath;
    
    @Value("${save.uploads.path}")
    private String uploadDir;

    

    @Autowired
    private VerificationConfigRepository verificationConfigRepository;


//	@Autowired
//	private AmazonS3 s3Client;

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	private static final int TAGS_PER_PAGE = 40;
	private static final int COLS = 10;   // ✅ from image
//	private static final int ROWS = 10;  // ✅ from image
	private static final float TAG_GAP_X_MM = 2.0f;   // horizontal gap
	private static final float TAG_GAP_Y_MM = 0f;   // vertical gap (your issue)

	private static final float TAG_W_MM = 18f;
	private static final float TAG_H_MM = 73f;
	private static final float FOLD_MM  = 35f;
	private static final float PAGE_MARGIN_MM = 3f;   // start after 3 mm
//	private static final float TAG_GAP_MM     = 2f;   // 1 mm gap between tags



//	private final String bucketName = "elasticbeanstalk-ap-south-1-012676044441"; // Replace with your bucket name
////	private final String region = "ap-south-1"; // Replace with your AWS region (e.g., "us-east-1")

	// Only logged-in users can add products
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/add-product")
	public String showAddProductPage() {

		return "forward:/index.html";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/remove-product")
	public String removeProductPage() {
		System.out.println("came inside remove");
		return "forward:/index.html";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/modify-product")
	public String modifyProductPage() {
		return "forward:/index.html";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/set-price")
	public String setPricePage() {
		return "forward:/index.html";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/view-product")
	public String viewProductPage() {
		return "forward:/index.html";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/load-product")
	public String loadPoductPage() {
		return "forward:/index.html";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/verify-product")
	public String verifyPoductPage() {
		return "forward:/index.html";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/custom-tags")
	public String verifCustomPage() {
		return "forward:/index.html";
	}

	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/get-estimate")
	public String getEstimatePage() {
		return "forward:/index.html";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/generate-report")
	public String getReportPage() {
		return "forward:/index.html";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/import-data")
	public String getImportDataPage() {
		return "forward:/index.html";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/permissions")
	public String permission() {
		return "forward:/index.html";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/enquiry-log")
	public String enquiryLog() {
		return "forward:/index.html";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/sales-log")
	public String salesLog() {
		return "forward:/index.html";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/batch-update")
	public String batchUpdate() {
		return "forward:/index.html";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/product-snapshot")
	public String productSnapshot() {
		return "forward:/index.html";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/estimate-snapshot")
	public String estimateSnapshot() {
		return "forward:/index.html";
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
	        return ResponseEntity.ok(categoryName);
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

	private BigDecimal scaleTo3(BigDecimal value) {
	    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
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
			 @RequestParam(value = "customFields", required = false) String customFieldsJson,
			 @RequestParam(value = "drLabourP", required = false) BigDecimal drLabourP,
				@RequestParam(value = "osLabourP", required = false) BigDecimal osLabourP,
				@RequestParam(value = "chainLabourP", required = false) BigDecimal chainLabourP,
				 @RequestParam(value = "vilandiLabourP", required = false) BigDecimal vilandiLabourP,
				 @RequestParam(value = "jadtarLabourP", required = false) BigDecimal jadtarLabourP,
				@RequestParam(value = "jadvilandi", required = false) BigDecimal jadvilandi,
				@RequestParam(value = "jadvilandiRate", required = false) BigDecimal jadvilandiRate
				) {

		try {
			Product product = new Product();
			// Process the image file
			String imageUrl = saveImageFile(imageFile);
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
			gross    = scaleTo3(gross);
		    net      = scaleTo3(net);
		    pearlsGm = scaleTo3(pearlsGm);

		    // if there are variants by category, normalize them too if needed
		    jadtarGross   = scaleTo3(jadtarGross);
		    jadtarNet     = scaleTo3(jadtarNet);
		    jadtarPearls  = scaleTo3(jadtarPearls);
		    chainNet      = scaleTo3(chainNet);
		    chainGross    = scaleTo3(chainGross);
		    diamondGross  = scaleTo3(diamondGross);
		    earringGross  = scaleTo3(earringGross);
		    vilandiGross  = scaleTo3(vilandiGross);
		    pearlsVilandi = scaleTo3(pearlsVilandi);
			// Set category-specific fields based on categoryId
			if (categoryId == 1 && (subCategoryId != 19 && subCategoryId !=20)) { 
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
				product.setLabourP(drLabourP);
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
				product.setLabourP(osLabourP);
			} else if (categoryId == 3) { // Chains
				product.setDesignNo(designNo);
				product.setNet(chainNet);
				product.setGross(chainGross);
				product.setLabour(chainLabour);
				product.setLabourAll(chainLabourAll);
				product.setLabourP(chainLabourP);
			} else if (subCategoryId == 19 || subCategoryId ==20) { // Diamond Earrings
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
				product.setLabourP(drLabourP);
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
				product.setLabourP(vilandiLabourP);
			} else if (categoryId == 5) { // Jadtar Register
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
				product.setLabourP(jadtarLabourP);
				product.setVilandiCt(jadvilandi);
				product.setvRate(jadvilandiRate);
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
			
			String qrUrl = baseUrl + "/loadProductByDesignNo/" + product.getDesignNo();

			// 2. Define save location
			Files.createDirectories(Paths.get(qrDir));
			String qrFileName = "QR_" + product.getDesignNo() + ".png";
			String qrFilePath = qrDir + qrFileName;

			// 3. Generate QR
			generateQRCodeImage(qrUrl, qrFilePath);
			
			String qr_path=qrPublicPath + qrFileName;
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
	}	@GetMapping("/loadProductByDesignNo/{designNo:.+}")
	public String getProductByDesignNo(@PathVariable("designNo") String designNo) {
	    return "forward:/index.html"; 
	}

	
	// GET current frequency
    @GetMapping("/frequency")
    public ResponseEntity<Map<String, Integer>> getFrequency() {
        int days = productService.getVerificationDays();
        return ResponseEntity.ok(Map.of("frequency", days));
    }

    // POST update frequency
    @PostMapping("/frequency")
    public ResponseEntity<String> setFrequency(@RequestBody FrequencyRequest request) {
    	productService.setVerificationDays(request.getFrequency());
        return ResponseEntity.ok("Verification frequency updated successfully");
    }
	
    @GetMapping("/available-order-ids")
    public ResponseEntity<List<String>> getAvailableOrderIds(
            @RequestParam(value = "categoryId", required = false) List<Integer> categoryIds) {
        
        List<String> availableOrderIds;
        
        if (categoryIds == null || categoryIds.isEmpty()) {
            // All available orders if no category specified
            availableOrderIds = productService.findAllByIsAssignedFalse()
                                             .stream()
                                             .map(Orders::getOrderId)
                                             .collect(Collectors.toList());
        } else {
            // Filter by one or more categoryIds
            availableOrderIds = productService.findByCategoryIdsAndIsAssignedFalse(categoryIds)
                                             .stream()
                                             .map(Orders::getOrderId)
                                             .collect(Collectors.toList());
        }
        
        return ResponseEntity.ok(availableOrderIds);
    }

	@PostMapping("/upload-image")
	public String saveImageFile(@RequestParam("imageFile") MultipartFile imageFile) {
	    try {
	        // Save directory
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

	@PostMapping("/remove/{productId}")
	public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable("productId") String productId) {
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
	public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable("categoryId") Long categoryId) {
		List<Product> products = productService.findProductsByCategoryId(categoryId);
		if (products.isEmpty()) {
			throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
		}
		return ResponseEntity.ok(products);
	}

	private void populateProductPriceAndStatus(Product product) {
		logger.info("[populateProductPriceAndStatus] Populating price/status for product id={}, designNo={}, name={}", 
				product.getProductId(), product.getDesignNo(), product.getItem());
		RateWrapper rateWrapper = new RateWrapper();
		List<Rate> rates = productService.getAllRates();
		logger.info("[populateProductPriceAndStatus] Loaded rates: size={}", rates != null ? rates.size() : 0);
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
	    
		int verificationFreqcy = getVerificationFrequency();
		boolean verified = isProductVerified(product, verificationFreqcy);
		if (verified) {
			logger.info("[populateProductPriceAndStatus] Product is verified");
			product.setVerificationStatus(1);
		} else {
			logger.info("[populateProductPriceAndStatus] Product is unverified");
			if (product.getVerificationStatus() != -1) {
				product.setVerificationStatus(0);
			}
		}
		Estimate e = new Estimate();
		List<BigDecimal> prices = calculateProductPrice(product, rateWrapper, e, rates);
		logger.info("[populateProductPriceAndStatus] Calculated prices: raw={}, withFields={}", prices.get(0), prices.get(1));
		product.setPrice(prices.get(0));
		product.setPriceWithFields(prices.get(1));
	}

	@GetMapping("loadProduct/{id:.+}")
	public ResponseEntity<Product> getProduct(@PathVariable("id") String id) {
		logger.info("[loadProduct] Fetching product by designNo: {}", id);
		Product product = productService.findProductById(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
		populateProductPriceAndStatus(product);
		logger.info("[loadProduct] Returning product: id={}, designNo={}, price={}, priceWithFields={}", 
				product.getProductId(), product.getDesignNo(), product.getPrice(), product.getPriceWithFields());
		return ResponseEntity.ok(product);
	}

	@GetMapping("loadProductDetail/{id}")
	public ResponseEntity<Product> getProduct(@PathVariable("id") Long id) {
		logger.info("[loadProductDetail] Fetching product by PK ID: {}", id);
		Product product = productService.findProductFromId(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
		populateProductPriceAndStatus(product);
		logger.info("[loadProductDetail] Returning product: id={}, designNo={}, price={}, priceWithFields={}", 
				product.getProductId(), product.getDesignNo(), product.getPrice(), product.getPriceWithFields());
		return ResponseEntity.ok(product);
	}

	 @GetMapping("/product/load")
	    public ResponseEntity<ProductPage> loadProducts(
	            @RequestParam("page") int page,
	            @RequestParam("size") int size,
	            @RequestParam(value = "category", required = false) String category,
	            @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
	            @RequestParam(value = "searchBy", required = false, defaultValue = "name") String searchBy, // <-- new
	            @RequestParam(value = "sortBy", required = false) String sortBy,
				@RequestParam(value = "verifiedOnly", required = false, defaultValue = "false") Boolean verifiedOnly,
				@RequestParam(value = "unverifiedOnly", required = false, defaultValue = "false") Boolean unverifiedOnly,
					HttpSession session) {
	System.out.println("search term is "+searchTerm+" and search by is "+searchBy);
	System.out.println("sort by is "+sortBy+" and verified only is "+verifiedOnly);
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
					/*
					* String prevSearchTerm = (String) session.getAttribute("prevSearchTerm");
					* 
					* @SuppressWarnings("unchecked") List<Integer> prevCategory = (List<Integer>)
					* session.getAttribute("prevCategory"); String prevSearchBy = (String)
					* session.getAttribute("prevSearchBy");
					*/
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
				} else if ("verifiedFirst".equalsIgnoreCase(sortBy)) {
    sort = Sort.by(Sort.Order.desc("verificationStatus"));
} 
else if ("unverifiedFirst".equalsIgnoreCase(sortBy)) {
    sort = Sort.by(Sort.Order.asc("verificationStatus"));
} 
				PageRequest pageable = PageRequest.of(page, size, sort);

				System.out.println("cateogries "+categories);
				// Execute search based on presence of category filter and searchBy
				Page<Product> products;
				System.out.println("Pageable: " + pageable);

				boolean dbVerifiedOnly = verifiedOnly && !unverifiedOnly;
				if (categories == null) {
					products = productService.searchWithoutCategory(searchTerm, searchBy, dbVerifiedOnly, pageable);
				} else {
					products = productService.searchByCategory(categories, searchTerm, searchBy, dbVerifiedOnly, pageable);
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
			//    LocalDateTime now = LocalDateTime.now();

				List<Product> processedProducts = new ArrayList<>();

for (Product product : products.getContent()) {

    List<BigDecimal> prices = calculateProductPrice(product, rateWrapper, e, rates);
    product.setPrice(prices.get(0));
    product.setPriceWithFields(prices.get(1));

    boolean verified = isProductVerified(product, verificationFreqcy);

    if (verified) {
        product.setVerificationStatus(1);
    } else {
        if (product.getVerificationStatus() != -1) {
            product.setVerificationStatus(0);
        }
    }

    if (unverifiedOnly) {
        if (product.getVerificationStatus() != 1) {
            processedProducts.add(product);
        }
    } else if (verifiedOnly) {
        if (product.getVerificationStatus() == 1) {
            processedProducts.add(product);
        }
    } else {
        processedProducts.add(product);
    }
}

// ✅ SORT BY VERIFIED (NEW)
if ("verifiedFirst".equalsIgnoreCase(sortBy)) {
    processedProducts.sort((p1, p2) ->
        Integer.compare(p2.getVerificationStatus(), p1.getVerificationStatus())
    );
} 
else if ("unverifiedFirst".equalsIgnoreCase(sortBy)) {
    processedProducts.sort((p1, p2) ->
        Integer.compare(p1.getVerificationStatus(), p2.getVerificationStatus())
    );
}
				long totalCount = (verifiedOnly || unverifiedOnly)
        ? processedProducts.size()   // filtered count
        : products.getTotalElements();

return ResponseEntity.ok(new ProductPage(processedProducts, totalCount));
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

			gold1 = nullSafe(rateWrapper.goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP));

			e.setGold(nullSafe(product.getNet()).multiply(gold1));
			
			if (product.getLabour() != null && product.getLabour().compareTo(BigDecimal.ZERO) != 0) {
				labour = nullSafe(product.getNet()).multiply(nullSafe(product.getLabour()));
			} else if(product.getLabourAll()!= null && product.getLabourAll().compareTo(BigDecimal.ZERO)!=0){
				labour = nullSafe(product.getLabourAll());
			}
			else {
				labour = nullSafe(e.getGold()).multiply(nullSafe(product.getLabourP()));
				labour = labour.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
			}
			e.setLabour(labour);
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
	public ResponseEntity<Product> updateProduct(@PathVariable("id") String id,
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
			@RequestParam(value = "pcsEarrings", required = false) Integer pcsEarrings,
			@RequestParam(value = "diamondsCtEarrings", required = false) BigDecimal diamondsCtEarrings,
			@RequestParam(value = "diamondsCtEarringsRate", required = false) BigDecimal diamondsCtEarringsRate,
			@RequestParam(value = "diaEOs", required = false) BigDecimal diaEOs,
			@RequestParam(value = "diaEOsRate", required = false) BigDecimal diaEOsRate,
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
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			@RequestParam(value = "karat", required = false) BigDecimal karat,
			@RequestParam(value = "labour", required = false) BigDecimal labour,
			@RequestParam(value = "labourAll", required = false) BigDecimal labourAll,
			@RequestParam(value = "ssosPearlCt", required = false) BigDecimal ssosPearlCt,
			@RequestParam(value = "ssosPearllbl", required = false) BigDecimal ssosPearllbl,
			@RequestParam(value = "customFields", required = false) String customFieldsJson,
			 @RequestParam(value = "labourPer", required = false) BigDecimal labourPer,
			@RequestParam(value = "jadvilandi", required = false) BigDecimal jadvilandi,
			@RequestParam(value = "jadvilandiRate", required = false) BigDecimal jadvilandiRate
			) {

		if (id == null || id.isEmpty()) {
			throw new ProductUpdateException("Product ID cannot be null or empty.");
		}

		if (categoryId == null) {
			throw new ProductUpdateException("Category ID is required.");
		}
		Product updatedProduct = new Product();
		updatedProduct.setItem(productName);
		updatedProduct.setPrice(productPrice);
		updatedProduct.setRemarks(productRemarks);
		updatedProduct.setImageUrl(productImageUrl);
		updatedProduct.setCategoryId(categoryId);
		updatedProduct.setSubCategoryId(subCategoryId);
		updatedProduct.setNet(productNet);
		updatedProduct.setGross(gross);
		updatedProduct.setKarat(karat);
		updatedProduct.setDesignNo(designNo);
		updatedProduct.setLabour(labour);
		updatedProduct.setLabourAll(labourAll);
		updatedProduct.setLabourP(labourPer);
		 if(customFieldsJson != null) {
			 updatedProduct.setCustomFields(customFieldsJson);
		    }
		// String design ="";
		if (categoryId == 1) {
			updatedProduct.setPcs(pcs);
			updatedProduct.setDiamondsCt(diaWeight);
			updatedProduct.setDiaRt(diaRate);
			updatedProduct.setOtherStonesCt(diaOs);
			updatedProduct.setOtherStonesRt(diaOsRate);
		}
		if (categoryId == 2) {
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
			updatedProduct.setOthers(others);
			updatedProduct.setSsPearlCt(ssosPearllbl);
			updatedProduct.setSsRate(ssosPearlCt);
		}
		if (categoryId == 4) {
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
		}
		if (categoryId == 5) {
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
			updatedProduct.setVilandiCt(jadvilandi);
			updatedProduct.setvRate(jadvilandiRate);
		}
		Product updated = productService.updateProduct(id, updatedProduct, imageFile,productOrderid);

		if (updated == null) {
			throw new ProductUpdateException("Failed to update product. Product not found with ID: " + id);
		}
		return ResponseEntity.ok(updated);
	}

	@GetMapping("/getEstimate/{productId}")
	public ResponseEntity<Map<String, Object>> getEstimate(@PathVariable("productId") Long productId) {
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
	public ResponseEntity<Page<Product>> getProductsByCategory(
	    @PathVariable("categoryId") Long categoryId,
	    @RequestParam(value = "subCategoryId", required = false) Long subCategoryId, // <--- Added
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

	    // Pass subCategoryId along with categoryId
	    Page<Product> products = productService.getFilteredProducts(categoryId, subCategoryId, startDate, endDate, page, size);

	    if (products.isEmpty()) {
	        throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
	    }
	    List<Rate> rates = productService.getAllRates();
	    RateWrapper rateWrapper = new RateWrapper();

	    for (Rate rate : rates) {
	        switch (rate.getCommodity().toLowerCase()) {
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
	        List<BigDecimal> prices = calculateProductPrice(product, rateWrapper, e, rates);
	        product.setPrice(prices.get(0));
	        product.setPriceWithFields(prices.get(1));
	    });
	    return products.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
	}

	
	@GetMapping("/getReportAll/{categoryId}")
	public ResponseEntity<List<Product>> getAllProductsByCategory(
	    @PathVariable("categoryId") Long categoryId,
	    @RequestParam(value = "subCategoryId", required = false) Long subCategoryId // <-- Added
	) {
	    List<Product> allProducts = productService.getAllProductsByCategory(categoryId, subCategoryId); // <-- Pass subCategoryId

	    if (allProducts.isEmpty()) {
	        throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
	    }

	    List<Rate> rates = productService.getAllRates();
	    RateWrapper rateWrapper = new RateWrapper();
	    for (Rate rate : rates) {
	        switch (rate.getCommodity().toLowerCase()) {
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
	    allProducts.forEach (product -> {
	        List<BigDecimal> prices = calculateProductPrice(product, rateWrapper, e, rates);
	        product.setPrice(prices.get(0));
	        product.setPriceWithFields(prices.get(1));
	    });

	    return ResponseEntity.ok(allProducts);
	}


	 @PutMapping("/verify/{designNo:.+}")
	    public ResponseEntity<Void> setVerification(
	            @PathVariable("designNo") String designNo,
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
	public ResponseEntity<String> deleteUser(@PathVariable("id") Integer id) {
		try {
			userService.deleteUser(id);
			return ResponseEntity.ok("User deleted successfully.");
		} catch (Exception e) {
			throw new UserDeletionException("Error deleting user: " + e.getMessage(), e);
		}
	}
	
	@GetMapping("/proxy-image")
	public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String url) {
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
	
	private static float mm(float mm) {
	    return mm * 72f / 25.4f; // PDF points
	}

	
	@GetMapping(
	        value = "/getTagsPdf/{categoryId}",
	        produces = MediaType.APPLICATION_PDF_VALUE
	)
	public ResponseEntity<byte[]> generateFoldedTagsPdf(
	        @PathVariable("categoryId") Long categoryId,
	        @RequestParam(value = "subCategoryId", required = false) Long subCategoryId,

	        // 👇 slot starts from 1 (user)
	        @RequestParam(value = "startSlot", defaultValue = "1") int startSlot,

	        // 👇 how many tags to print
	        @RequestParam(value = "count", required = false) Integer count,

	        // 👇 NEW: font size from UI
	        @RequestParam(value = "fontSize", defaultValue = "5.5") float fontSize
	) throws IOException {



System.out.println("we are inside generateFoldedTagsPdf");
			  List<Product> allProducts = productService.getAllProductsByCategory(categoryId, subCategoryId); // <-- Pass subCategoryId

			    if (allProducts.isEmpty()) {
			        throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
			    }

			    List<Rate> rates = productService.getAllRates();
			    RateWrapper rateWrapper = new RateWrapper();
			    for (Rate rate : rates) {
			        switch (rate.getCommodity().toLowerCase()) {
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
			        List<BigDecimal> prices = calculateProductPrice(product, rateWrapper, e, rates);
			        product.setPrice(prices.get(0));
			        product.setPriceWithFields(prices.get(1));
			    });
			    int startSlotIndex = Math.max(0, startSlot - 1);

			 // If count not provided → print all
			 int printCount = (count == null || count <= 0)
			         ? allProducts.size()
			         : Math.min(count, allProducts.size());

			 byte[] pdfBytes =
				        generateFoldedTagPdf(allProducts, startSlotIndex, printCount,fontSize);


			        return ResponseEntity.ok()
			            .header(HttpHeaders.CONTENT_DISPOSITION,
			                    "inline; filename=folded-tags.pdf")
			            .contentType(MediaType.APPLICATION_PDF)
			            .body(pdfBytes);
		}
		
		private byte[] generateFoldedTagPdf(
		        List<Product> products,
		        int startSlotIndex,   // 0-based
		        int printCount,
		        float fontSize// how many products to print
		) throws IOException {



		    PDDocument doc = new PDDocument();

		    // 🔒 Load Unicode font (₹ safe)
		    InputStream fontStream =
		        ProductController.class.getResourceAsStream(
		            "/fonts/NotoSans-Regular.ttf");

		    if (fontStream == null) {
		    	doc.close();
		        throw new RuntimeException("Font not found in resources/fonts/");
		    }

		    PDFont font = PDType0Font.load(doc, fontStream, true);

		 // ✅ Load Bold font
		 InputStream boldFontStream =
		         ProductController.class.getResourceAsStream(
		                 "/fonts/NotoSans-Bold.ttf");

		 if (boldFontStream == null) {
		     doc.close();
		     throw new RuntimeException("Bold font not found in resources/fonts/");
		 }

		 PDFont boldFont = PDType0Font.load(doc, boldFontStream, true);


		    PDPage page = null;
		    PDPageContentStream cs = null;

		    int productPointer = 0;

		    // ✅ FIX START
		    int slotsNeeded = startSlotIndex + printCount;
		    int totalSlots =
		            ((slotsNeeded + TAGS_PER_PAGE - 1) / TAGS_PER_PAGE)
		            * TAGS_PER_PAGE;
		    // ✅ FIX END

		    for (int slot = 0; slot < totalSlots; slot++) {

		        // New page every 40 slots
		        if (slot % TAGS_PER_PAGE == 0) {
		            if (cs != null) cs.close();

		            page = new PDPage(PDRectangle.A4);
		            doc.addPage(page);
		            cs = new PDPageContentStream(doc, page);
		        }

		        // Leave empty slots before startSlot
		        if (slot < startSlotIndex) continue;

		        // Stop if required count reached
		        if (productPointer >= printCount) continue;

		        int pos = slot % TAGS_PER_PAGE;
		        int col = pos % COLS;
		        int row = pos / COLS;

		        float startX = mm(PAGE_MARGIN_MM);
		        float startY = PDRectangle.A4.getHeight() - mm(PAGE_MARGIN_MM);

		        float x = startX + col * mm(TAG_W_MM + TAG_GAP_X_MM);
		        float y = startY - row * mm(TAG_H_MM + TAG_GAP_Y_MM);

		        drawSingleTag(
		        	    cs,
		        	    doc,
		        	    font,
		        	    boldFont, // ✅ new
		        	    products.get(productPointer),
		        	    x,
		        	    y,
		        	    fontSize
		        	);



		        productPointer++;
		    }


		    if (cs != null) cs.close();

		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    doc.save(out);
		    doc.close();

		    return out.toByteArray();
		}

private void drawSingleTag(
        PDPageContentStream cs,
        PDDocument doc,
        PDFont font,
        PDFont boldFont,
        Product p,
        float x,
        float y,
        float fontSize
) throws IOException {

    float padding = 3f;
    float lineGap = fontSize + 1.5f;

    float upperBoxY = y - padding;

    List<String> lines = new ArrayList<>();

    // ===== BASE DATA =====
    lines.add("G - " + p.getGross() + "  N - " + p.getNet() + " (" + fmtRate(p.getKarat()) + "K)");

    if (p.getDiamondsCt() != null && p.getDiaRt() != null)
        lines.add("Diamond- " + p.getDiamondsCt() + " - ₹" + fmtRate(p.getDiaRt()));

    if (p.getStones() != null && p.getStRate() != null)
        lines.add("Stones- " + fmtBRate(p.getStones()) + " - ₹" + fmtRate(p.getStRate()));

    if (p.getVilandiCt() != null && p.getvRate() != null)
        lines.add("Vilandi- " + p.getVilandiCt() + " - ₹" + fmtRate(p.getvRate()));

    if (p.getBeadsCt() != null && p.getBdRate() != null)
        lines.add("Beads- " + p.getBeadsCt() + " - ₹" + fmtRate(p.getBdRate()));

    if (p.getPearlsGm() != null && p.getPrlRate() != null)
        lines.add("Pearls- " + p.getPearlsGm() + " - ₹" + fmtRate(p.getPrlRate()));

    if (p.getSsPearlCt() != null && p.getSsRate() != null)
        lines.add("SS Pearl- " + p.getSsPearlCt() + " - ₹" + fmtRate(p.getSsRate()));

    if (p.getOtherStonesCt() != null && p.getOtherStonesRt() != null)
        lines.add("Other Stones-" + p.getOtherStonesCt() + " - ₹" + fmtRate(p.getOtherStonesRt()));

	    if (p.getMozonite() != null && p.getmRate() != null)
        lines.add("Mozonite-" + p.getMozonite() + " - ₹" + fmtRate(p.getmRate()));

    // ===== CUSTOM FIELDS =====
    String customFieldsJson = p.getCustomFields();
    if (customFieldsJson != null && !customFieldsJson.isEmpty() && !customFieldsJson.equals("{}")) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Map<String, String>> extras = mapper.readValue(
                    customFieldsJson,
                    new TypeReference<Map<String, Map<String, String>>>() {}
            );

            extras.forEach((name, values) -> {
                String qty = values.getOrDefault("qty", "");
                String rate = values.getOrDefault("rate", "");

                if (!qty.isBlank() || !rate.isBlank()) {
                    lines.add(name + "- " + qty + " - ₹" + rate);
                }
            });

        } catch (Exception ex) {
            System.err.println("Error parsing custom fields: " + ex.getMessage());
        }
    }

    // ===== FITTING / REAL STONE =====
    if (p.getFitting() != null || p.getRealStone() != null) {
        StringBuilder sb = new StringBuilder();

        if (p.getFitting() != null)
            sb.append("Ft - ₹").append(fmtRate(p.getFitting()));

        if (p.getRealStone() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("RS - ₹").append(fmtRate(p.getRealStone()));
        }

        lines.add(sb.toString());
    }

    // ===== SPLIT =====
    List<String> upperLines = new ArrayList<>();
    List<String> lowerLines = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
        if (i < 7) upperLines.add(lines.get(i));
        else lowerLines.add(lines.get(i));
    }

    // ================= UPPER =================
    float textX = x + mm(TAG_W_MM) / 2 + 18;

    cs.beginText();
    cs.setTextMatrix(Matrix.getRotateInstance(-Math.PI / 2, textX, upperBoxY));

    for (int i = 0; i < upperLines.size(); i++) {
        cs.setFont(i == 0 ? boldFont : font, fontSize);
        cs.showText(upperLines.get(i));
        cs.newLineAtOffset(0, -lineGap);
    }

    cs.endText();

    // ================= FOLD =================
    cs.moveTo(x + 2, y - mm(FOLD_MM));
    cs.lineTo(x + mm(TAG_W_MM) - 2, y - mm(FOLD_MM));
    cs.stroke();

    // ================= LOWER =================

    float currentY = y - mm(FOLD_MM) - mm(3);

    // ===== QR CALC =====
    float qrMargin = 0.5f;
    float qrSize = mm(TAG_W_MM - qrMargin * 2);
    float qrBottomY = y - mm(TAG_H_MM) + mm(qrMargin);
    float qrTopY = qrBottomY + qrSize + mm(2);

    cs.beginText();
    cs.setFont(font, fontSize);

    cs.setTextMatrix(
            Matrix.getRotateInstance(
                    -Math.PI / 2,
                    textX,   // SAME COLUMN (fix alignment)
                    currentY
            )
    );

    // 🔥 LOWER LINES
    for (String line : lowerLines) {

        if (currentY <= qrTopY + lineGap) break;

        cs.showText(line);
        cs.newLineAtOffset(0, -lineGap);
        currentY -= lineGap;
    }

    // 🔥 ORDER TEXT (same flow, no gap)
    if (currentY > qrTopY + lineGap) {
        cs.showText(p.getOrders().getOrderId() + "/" + p.getDesignNo());
    }

    cs.endText();

    // ================= QR =================
    String qrUrl = baseUrl + "/loadProductByDesignNo/" + p.getDesignNo();

    byte[] qrPng = generateQrPngBytes(qrUrl);
    PDImageXObject qrImg =
            PDImageXObject.createFromByteArray(doc, qrPng, "qr");

    float qrX = x + mm(qrMargin);

    cs.drawImage(qrImg, qrX, qrBottomY, qrSize, qrSize);
}


		private static String fmtRate(BigDecimal v) {
		    if (v == null) return "";
		    return v.stripTrailingZeros().toPlainString();
		}

		private static final DecimalFormat DF = new DecimalFormat("#.##");

		private String fmtBRate(BigDecimal val) {
		    return DF.format(val);
		}
		

		private String fmtDRate(double val) {
		    return DF.format(val);
		}
		 
		 private static byte[] generateQrPngBytes(String text) {
			    try {
			        BitMatrix matrix = new MultiFormatWriter()
			            .encode(text, BarcodeFormat.QR_CODE, 300, 300);

			        ByteArrayOutputStream out = new ByteArrayOutputStream();
			        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			        return out.toByteArray();

			    } catch (Exception e) {
			        throw new RuntimeException("QR generation failed", e);
			    }
			}

		 @PostMapping(
				    value = "/tags/custom/pdf",
				    produces = MediaType.APPLICATION_PDF_VALUE
				)
				public ResponseEntity<byte[]> generateCustomTagsPdf(
				        @RequestBody CustomTagRequest request
				) throws IOException {

				    if (request.getDesignNos() == null || request.getDesignNos().isEmpty()) {
				        throw new IllegalArgumentException("No design numbers provided");
				    }

				    int startSlotIndex = Math.max(0, request.getStartSlot() - 1);

				    // 1️⃣ Fetch products (unordered)
				    List<Product> fetchedProducts =
				            productService.getProductsByDesignNos(request.getDesignNos());

				    if (fetchedProducts.isEmpty()) {
				        throw new RuntimeException("No products found for given design numbers");
				    }

				    // 2️⃣ Preserve frontend order
				    Map<String, Product> productMap = fetchedProducts.stream()
				            .collect(Collectors.toMap(
				                    Product::getDesignNo,
				                    p -> p
				            ));

				    List<Product> orderedProducts = new ArrayList<>();
				    for (String dn : request.getDesignNos()) {
				        Product p = productMap.get(dn);
				        if (p != null) orderedProducts.add(p);
				    }

				    if (orderedProducts.isEmpty()) {
				        throw new RuntimeException("No valid products matched design numbers");
				    }

				    // 3️⃣ Load rates (UNCHANGED)
				    List<Rate> rates = productService.getAllRates();
				    RateWrapper rateWrapper = new RateWrapper();

				    for (Rate rate : rates) {
				        switch (rate.getCommodity().toLowerCase()) {
				            case "diamond" -> rateWrapper.diamondPrice = rate.getPrice();
				            case "gst" -> rateWrapper.gst = rate.getPrice();
				            case "silver" -> rateWrapper.silver = rate.getPrice();
				        }
				    }

				    // 4️⃣ Calculate prices (UNCHANGED)
				    Estimate estimate = new Estimate();
				    orderedProducts.forEach(product -> {
				        List<BigDecimal> prices =
				                calculateProductPrice(product, rateWrapper, estimate, rates);
				        product.setPrice(prices.get(0));
				        product.setPriceWithFields(prices.get(1));
				    });
				    
				    float fontSize = (request.getFontSize() != null && request.getFontSize() > 0)
				            ? request.getFontSize()
				            : 5.5f;   // default size

				    // 5️⃣ Generate PDF (UNCHANGED)
				    byte[] pdfBytes = generateFoldedTagPdf(
				            orderedProducts,
				            startSlotIndex,
				            orderedProducts.size(),
				            fontSize
				    );

				    return ResponseEntity.ok()
				            .header(HttpHeaders.CONTENT_DISPOSITION,
				                    "inline; filename=custom-folded-tags.pdf")
				            .contentType(MediaType.APPLICATION_PDF)
				            .body(pdfBytes);
				}
		
		@PreAuthorize("hasRole('ADMIN')")
		@PutMapping("/batch-update")
		public ResponseEntity<Map<String, Object>> batchUpdate(@RequestBody BatchUpdateRequest request) {
			Map<String, Object> response = new HashMap<>();
			int updatedCount = productService.applyBatchUpdate(request.getCategoryId(), request.getUpdates());
			response.put("updatedCount", updatedCount);
			response.put("success", true);
			return ResponseEntity.ok(response);
		}
	
	@GetMapping("/rate-history/{commodity}")
	public ResponseEntity<List<RateHistory>> getRateHistory(
			@PathVariable("commodity") String commodity,
			@RequestParam(value = "period", required = false) String period,
			@RequestParam(value = "from", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(value = "to", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = null;
		LocalDateTime end = null;

		if (period != null && !period.isBlank()) {
			start = resolvePeriodStart(period.trim(), now);
			end = now;
		} else if (from != null) {
			start = from.atStartOfDay();
			end = (to != null) ? to.plusDays(1).atStartOfDay().minusNanos(1) : now;
		}

		List<RateHistory> history = productService.getRateHistory(commodity, start, end);
		return ResponseEntity.ok(history);
	}

	private LocalDateTime resolvePeriodStart(String period, LocalDateTime now) {
		if (period.length() < 2) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period format.");
		}

		char unit = Character.toLowerCase(period.charAt(period.length() - 1));
		String numberPart = period.substring(0, period.length() - 1);
		int amount;
		try {
			amount = Integer.parseInt(numberPart);
		} catch (NumberFormatException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period format.");
		}

		return switch (unit) {
			case 'y' -> now.minusYears(amount);
			case 'm' -> now.minusMonths(amount);
			case 'd' -> now.minusDays(amount);
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid period unit.");
		};
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/price-history")
	public String priceHistoryPage() {
		return "forward:/index.html";
	}
	
	@GetMapping("/public-rates")
	public String publicRatesPage() {
		return "forward:/index.html";
	}
}


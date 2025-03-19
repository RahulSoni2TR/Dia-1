package com.example.webapp.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
import com.example.webapp.models.Estimate;
import com.example.webapp.models.Product;
import com.example.webapp.models.ProductPage;
import com.example.webapp.service.ProductService;
import com.example.webapp.service.UserService;

import jakarta.servlet.http.HttpSession;

import com.example.webapp.models.Rate;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.webapp.models.RateWrapper;
import com.example.webapp.models.UserRoleUpdate;
import com.example.webapp.models.UserTemp;

@Controller
public class ProductController {

	@Autowired
	private UserService userService;

	@Autowired
	private ProductService productService;

	@Autowired
	private AmazonS3 s3Client;

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

	private final String bucketName = "elasticbeanstalk-ap-south-1-012676044441"; // Replace with your bucket name
//	private final String region = "ap-south-1"; // Replace with your AWS region (e.g., "us-east-1")

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

	@PostMapping("/add")
	public ResponseEntity<Map<String, Object>> addProduct(@RequestParam("productName") String productName,
			@RequestParam(value = "price", required = false) BigDecimal price,
			@RequestParam("stockQuantity") Integer stockQuantity, @RequestParam("categoryId") Integer categoryId,
			@RequestParam("imageUrl") MultipartFile imageFile,

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
			@RequestParam(value = "jadtarLabourAll", required = false) BigDecimal jadtarLabourAll) {

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
			product.setImageUrl(imageUrl);// Set the image URL
			product.setRemarks(remarks);
			product.setKarat(karat);

			// Set category-specific fields based on categoryId
			if (categoryId == 1) { // Diamond Rings
				product.setNet(net);
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
				product.setOtherStonesCt(otherStonesCt);
				product.setDesignNo(designNoOS);
				product.setLabour(osLabour);
				product.setLabourAll(osLabourAll);
				product.setOtherStonesCt(otherStonesCt);
				product.setOtherStonesRt(openStRate);
			} else if (categoryId == 3) { // Chains
				product.setDesignNo(designNo);
				product.setNet(chainNet);
				product.setLabour(chainLabour);
				product.setLabourAll(chainLabourAll);
			} else if (categoryId == 4) { // Diamond Earrings
				product.setDesignNo(designNoEarring);
				product.setNet(earringNet);
				product.setPcs(earringPcs);
				product.setDiamondsCt(diamondWeightEarring);
				product.setDiaRt(diamondsWtRate);
				product.setLabour(diamondLabour);
				product.setLabourAll(diamondLabourAll);
				product.setOtherStonesCt(earSt);
				product.setOtherStonesRt(earStRate);
			} else if (categoryId == 5) { // Vilandi
				product.setDesignNo(designNoVilandi);
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
			} else if (categoryId == 6) { // Jadtar Register
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

			// Add the product using the service
			productService.addProduct(product);

			Map<String, Object> response = new HashMap<>();
			response.put("message", "Product added successfully!");
			response.put("success", true);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			logger.error("Error occurred while adding product: {}", productName, e);
			throw new AddProductException("Error occurred while adding product: " + productName, e);
		}
	}

	/*
	 * local image store private String saveImageFile(MultipartFile imageFile) { try
	 * { // Specify the directory to save the images String uploadDir =
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

	public String saveImageFile(MultipartFile file) {
		try {
			File fileObj = convertMultiPartFileToFile(file);
			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
			s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj)
					.withCannedAcl(CannedAccessControlList.PublicRead));
			fileObj.delete();
			return s3Client.getUrl(bucketName, fileName).toString();
		} catch (Exception e) {
			throw new FileUploadException("Error uploading file: " + file.getOriginalFilename(), e);
		}
	}

	private File convertMultiPartFileToFile(MultipartFile file) {
		File convertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			throw new FileConversionException("Error converting multipartFile to file: " + file.getOriginalFilename(),
					e);
		}
		return convertedFile;
	}

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
		return ResponseEntity.ok(product);
	}

	@GetMapping("/product/load")
	public ResponseEntity<ProductPage> loadProducts(@RequestParam int page, @RequestParam int size,
			@RequestParam(required = false) Integer category,
			@RequestParam(required = false, defaultValue = "") String searchTerm, String sortBy, HttpSession session) {
		if (page < 0 || size <= 0) {
			throw new InvalidPaginationException("Page index must be 0 or greater, and size must be greater than 0.");
		}

		Page<Product> products;
		RateWrapper rateWrapper = new RateWrapper();

		try {
			// Fetch previous search from session
			String prevSearchTerm = (String) session.getAttribute("prevSearchTerm");
			Integer prevCategory = (Integer) session.getAttribute("prevCategory");

			// If new search is different from the previous one, reset page to 0
			if (!searchTerm.equals(prevSearchTerm) || !Objects.equals(category, prevCategory)) {
				page = 0; // Reset pagination
			}
			// Store current search values in session
			session.setAttribute("prevSearchTerm", searchTerm);
			session.setAttribute("prevCategory", category);
		} catch (Exception e) {
			throw new SessionHandlingException("Error accessing session attributes.");
		}
		Sort sort = Sort.unsorted(); // No sorting by default

		if ("old".equals(sortBy)) {
			sort = Sort.by(Sort.Order.asc("createDateTime")); // Ascending for Old Products
		} else if ("recent".equals(sortBy)) {
			sort = Sort.by(Sort.Order.desc("createDateTime")); // Descending for Most Recent
		}
		if (category == null) {
			products = productService.findWithoutCategory(category, PageRequest.of(page, size, sort), searchTerm);
		} else {
			products = productService.findByCategory(category, PageRequest.of(page, size, sort), searchTerm);
		}
		if (products.isEmpty()) {
			throw new ProductNotFoundException("No products found for the given filters.");
		}
		List<Rate> rates = productService.getAllRates();

		// Fetch rates for calculations

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
			BigDecimal calculatedPrice = calculateProductPrice(product, rateWrapper, e, rates);
			product.setPrice(calculatedPrice);
			System.out.println("item name " + product.getItem());
		});

		return ResponseEntity.ok(new ProductPage(products.getContent(), products.getTotalElements()));
	}

	private BigDecimal calculateProductPrice(Product product, RateWrapper rateWrapper, Estimate e, List<Rate> r) {
		try {
			BigDecimal labour = null;
			BigDecimal gold1 = null;
			BigDecimal estimate_gst = null;
			BigDecimal estimate_nogst = null;
			BigDecimal total = null;

			if (nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(24)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(22)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(18)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(14)) == 0
					|| nullSafe(product.getKarat()).compareTo(BigDecimal.valueOf(10)) == 0) {

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

			estimate_gst = nullSafe(
					estimate_nogst.multiply(rateWrapper.gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP));

			e.setGst(estimate_gst);

			total = nullSafe(estimate_nogst.add(estimate_gst));

			e.setTotal(total);
			return e.getTotal() != null ? e.getTotal() : BigDecimal.valueOf(0);
		} catch (ArithmeticException ex) {
			throw new InvalidCalculationException("Error occurred while calculating product price", ex);
		}
	}

	@PutMapping(value = "/updateProduct/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<Product> updateProduct(@PathVariable String id,
			@RequestParam(value = "productName", required = false) String productName,
			@RequestParam(value = "productPrice", required = false) BigDecimal productPrice,
			@RequestParam(value = "productRemarks", required = false) String productRemarks,
			@RequestParam(value = "productImageUrl", required = false) String productImageUrl,
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
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
			@RequestParam(value = "labourAll", required = false) BigDecimal labourAll) {

		if (id == null || id.isEmpty()) {
			throw new ProductUpdateException("Product ID cannot be null or empty.");
		}

		if (categoryId == null) {
			throw new ProductUpdateException("Category ID is required.");
		}
		Product updatedProduct = new Product();
		// String design ="";
		if (categoryId == 1) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setNet(productNet);
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
			// design=designNo;
		}
		if (categoryId == 3) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setDesignNo(designNo);
			updatedProduct.setNet(productNet);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			// design=designNo;
		}
		if (categoryId == 4) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setDesignNo(designNoEarrings);
			updatedProduct.setNet(productNet);
			updatedProduct.setPcs(pcsEarrings);
			updatedProduct.setDiamondsCt(diamondsCtEarrings);
			updatedProduct.setDiaRt(diamondsCtEarringsRate);
			updatedProduct.setOtherStonesCt(diaEOs);
			updatedProduct.setOtherStonesRt(diaEOsRate);
			updatedProduct.setLabour(labour);
			updatedProduct.setLabourAll(labourAll);
			updatedProduct.setKarat(karat);
			// design=designNoEarrings;
		}
		if (categoryId == 5) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
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
		if (categoryId == 6) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
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

		Product updated = productService.updateProduct(id, updatedProduct, imageFile);

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
			case "gold":
				rateWrapper.goldPrice = rate.getPrice();
				break;
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
		BigDecimal calculatedPrice = calculateProductPrice(product, rateWrapper, e, rates);
		product.setPrice(calculatedPrice);
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
	public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable("categoryId") Long categoryId,
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

		List<Product> products = productService.getFilteredProducts(categoryId, startDate, endDate, page, size);
		if (products.isEmpty()) {
			throw new CategoryNotFoundException("No products found for category ID: " + categoryId);
		}
		List<Rate> rates = productService.getAllRates();
		RateWrapper rateWrapper = new RateWrapper();
		// Fetch rates for calculations

		for (Rate rate : rates) {
			switch (rate.getCommodity().toLowerCase()) {
			case "gold":
				rateWrapper.goldPrice = rate.getPrice();
				break;
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
			BigDecimal calculatedPrice = calculateProductPrice(product, rateWrapper, e, rates);
			product.setPrice(calculatedPrice);
		});
		return products.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
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

}

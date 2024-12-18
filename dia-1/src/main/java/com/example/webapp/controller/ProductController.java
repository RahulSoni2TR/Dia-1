package com.example.webapp.controller;

import java.io.File;
import java.io.FileOutputStream;
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
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.webapp.models.Estimate;
import com.example.webapp.models.Product;
import com.example.webapp.models.ProductPage;
import com.example.webapp.service.ProductService;
import com.example.webapp.models.Rate;
import com.example.webapp.models.RateWrapper;

@Controller
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private AmazonS3 s3Client;

	private final String bucketName = "elasticbeanstalk-ap-south-1-012676044441"; // Replace with your bucket name
	private final String region = "ap-south-1"; // Replace with your AWS region (e.g., "us-east-1")

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
	public String getReport() {
		return "get_report"; // Return the view_product.html view
	}

	@PostMapping("/add")
	public ResponseEntity<Map<String, Object>> addProduct(@RequestParam("productName") String productName,
			@RequestParam("price") BigDecimal price, @RequestParam("stockQuantity") Integer stockQuantity,
			@RequestParam("categoryId") Integer categoryId, @RequestParam("imageUrl") MultipartFile imageFile,

			// Category-specific fields
			@RequestParam(value = "net", required = false) BigDecimal net,
			@RequestParam(value = "chainNet", required = false) BigDecimal chainNet,
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
			@RequestParam(value = "designNoOS", required = false) String designNoOS,
			@RequestParam(value = "designNoDR", required = false) String designNoDR,
			@RequestParam(value = "designNo", required = false) String designNo,
			@RequestParam(value = "designNoEarring", required = false) String designNoEarring,
			@RequestParam(value = "designNoVilandi", required = false) String designNoVilandi,
			@RequestParam(value = "designNoJadtar", required = false) String designNoJadtar,
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
			@RequestParam(value = "jadtarFitting", required = false) BigDecimal jadtarFittingRate,
			@RequestParam(value = "mozStone", required = false) BigDecimal mozStone,
			@RequestParam(value = "mozStoneRate", required = false) BigDecimal mozStoneRate) {

		// Process the image file
		String imageUrl = saveImageFile(imageFile);
		System.out.println(imageUrl);
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
			product.setDiamondsCt(diaWeight);
			product.setDesignNo(designNoDR);
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
			product.setDesignNo(designNoOS);
		} else if (categoryId == 3) { // Chains
			product.setDesignNo(designNo);
			product.setNet(chainNet);
		} else if (categoryId == 4) { // Diamond Earrings
			product.setDesignNo(designNoEarring); 
			product.setNet(earringNet);
			product.setPcs(earringPcs);
			product.setDiamondsCt(diamondWeightEarring);
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
		} else if (categoryId == 6) { // Jadtar Register
			System.out.println("Design is "+designNoJadtar);
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
		}

		// Add the product using the service
		productService.addProduct(product);

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Product added successfully!");
		response.put("success", true);
		return ResponseEntity.ok(response);
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
		File fileObj = convertMultiPartFileToFile(file);
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		s3Client.putObject(
				new PutObjectRequest(bucketName, fileName, fileObj).withCannedAcl(CannedAccessControlList.PublicRead));
		fileObj.delete();
		return s3Client.getUrl(bucketName, fileName).toString();
	}

	private File convertMultiPartFileToFile(MultipartFile file) {
		File convertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			System.out.print("Error converting multipartFile to file");
		}
		return convertedFile;
	}

	@PostMapping("/remove/{productId}")
	public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable String productId) {
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
	public ResponseEntity<Product> getProduct(@PathVariable String id) {
		Optional<Product> product = productService.findProductById(id);
		return product.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}
	
	@GetMapping("loadProductDetail/{id}")
	public ResponseEntity<Product> getProduct(@PathVariable Long id) {
		Optional<Product> product = productService.findProductFromId(id);
		return product.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/product/load")
	public ResponseEntity<ProductPage> loadProducts(@RequestParam int page, @RequestParam int size,
			@RequestParam(required = false) Integer category) {
		Page<Product> products;
		// Page<Product> productPage = productService.findByCategory(category,
		// PageRequest.of(page, size));
		RateWrapper rateWrapper = new RateWrapper();
		Map<String, Object> response = new HashMap<>();
		if (category == null) {
			products = productService.findAll(PageRequest.of(page, size));
			List<Product> productsAll = products.getContent();
			List<Rate> rates = productService.getAllRates();

			// Fetch rates for calculations

			for (Rate rate : rates) {
				switch (rate.getCommodity().toLowerCase()) {
				case "gold":
					rateWrapper.goldPrice = rate.getPrice();
					break;
				case "diamond":
					rateWrapper.diamondPrice = rate.getPrice();
					break;
				case "labour":
					rateWrapper.labourPrice = rate.getPrice();
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
				BigDecimal calculatedPrice = calculateProductPrice(product, rateWrapper, e);
				product.setPrice(calculatedPrice);
			});
		} else {
			products = productService.findByCategory(category, PageRequest.of(page, size));
			List<Product> productsAll = products.getContent();
			List<Rate> rates = productService.getAllRates();

			// Fetch rates for calculations

			for (Rate rate : rates) {
				switch (rate.getCommodity().toLowerCase()) {
				case "gold":
					rateWrapper.goldPrice = rate.getPrice();
					break;
				case "diamond":
					rateWrapper.diamondPrice = rate.getPrice();
					break;
				case "labour":
					rateWrapper.labourPrice = rate.getPrice();
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
				BigDecimal calculatedPrice = calculateProductPrice(product, rateWrapper, e);
				product.setPrice(calculatedPrice);
			});

		}
		return ResponseEntity.ok(new ProductPage(products.getContent(), products.getTotalElements()));
	}

	private BigDecimal calculateProductPrice(Product product, RateWrapper rateWrapper, Estimate e) {
		BigDecimal estimateNoGst = BigDecimal.ZERO;
		BigDecimal goldPrice = null;
		BigDecimal diamondPrice = null;
		BigDecimal vilandiPrice = null;
		BigDecimal labour = null;
		BigDecimal gold1 = null;
		BigDecimal gst = null;
		BigDecimal estimate_gst = null;
		BigDecimal estimate_nogst = null;
		BigDecimal total = null;
		System.out.println("GST rate found: " + rateWrapper.gst);
		System.out.println("Gold rate found: " + rateWrapper.goldPrice);
		if (product.getCategoryId() == 6) {
			labour = product.getNet().multiply(rateWrapper.labourPrice);
			e.setLabour(labour);
			gold1 = rateWrapper.goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP);

			e.setGold(nullSafe(product.getNet()).multiply(gold1));
			System.out.println(e.getGold());
			e.setStones(nullSafe(product.getStones()).multiply(nullSafe(product.getStRate())));
			System.out.println(e.getStones());
			e.setBeads(nullSafe(product.getBeadsCt()).multiply(nullSafe(product.getBdRate())));
			System.out.println(e.getBeads());
			e.setPearls(nullSafe(product.getPearlsGm()).multiply(nullSafe(product.getPrlRate())));
			System.out.println(e.getPearls());
			e.setSsPearls(nullSafe(product.getSsPearlCt()).multiply(nullSafe(product.getSsRate())));
			System.out.println(e.getSsPearls());
			e.setMozo(nullSafe(product.getMozonite()).multiply(nullSafe(product.getmRate())));
			System.out.println(e.getMozo());
			e.setRealStones(nullSafe(product.getRealStone()));
			System.out.println(e.getRealStones());
			e.setFitting(nullSafe(product.getFitting()));
			System.out.println(e.getFitting());
			estimate_nogst = e.getGold().add(e.getLabour()).add(e.getStones()).add(e.getBeads()).add(e.getPearls())
					.add(e.getSsPearls()).add(e.getMozo()).add(e.getRealStones()).add(e.getFitting());
			System.out.println(estimate_nogst);
			e.setNogsttotal(estimate_nogst);
			// System.out.println("gst value is "+gst);
			estimate_gst = estimate_nogst.multiply(rateWrapper.gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
			e.setGst(estimate_gst);
			System.out.println(estimate_gst);
			total = estimate_nogst.add(estimate_gst);
			System.out.println("calculate price " + total);

			e.setTotal(total);
			return e.getTotal();
		}

		if (product.getCategoryId() == 5) {
			labour = product.getNet().multiply(rateWrapper.labourPrice);
			e.setLabour(labour);

			gold1 = rateWrapper.goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP);

			e.setGold(nullSafe(product.getNet()).multiply(gold1));
			e.setStones(nullSafe(product.getStones()).multiply(nullSafe(product.getStRate())));
			e.setBeads(nullSafe(product.getBeadsCt()).multiply(nullSafe(product.getBdRate())));
			e.setPearls(nullSafe(product.getPearlsGm()).multiply(nullSafe(product.getPrlRate())));
			e.setSsPearls(nullSafe(product.getSsPearlCt()).multiply(nullSafe(product.getSsRate())));
			e.setVilandi(nullSafe(product.getVilandiCt()).multiply(nullSafe(product.getvRate())));
			e.setMozo(nullSafe(product.getMozonite()).multiply(nullSafe(product.getmRate())));
			e.setRealStones(nullSafe(product.getRealStone()));
			e.setFitting(nullSafe(product.getFitting()));

			estimate_nogst = e.getGold().add(e.getLabour()).add(e.getStones()).add(e.getBeads()).add(e.getPearls())
					.add(e.getSsPearls()).add(e.getVilandi()).add(e.getMozo()).add(e.getRealStones())
					.add(e.getFitting());
			System.out.println(estimate_nogst);
			estimate_gst = estimate_nogst.multiply(rateWrapper.gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
			System.out.println(estimate_gst);
			e.setGst(estimate_gst);

			total = estimate_nogst.add(estimate_gst);
			System.out.println(total);
			e.setTotal(total);
			return e.getTotal();

		}
		return BigDecimal.valueOf(0);
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
			@RequestParam(value = "gross", required = false) BigDecimal gross,

			// Category-specific fields
			@RequestParam(value = "vilandiCt", required = false) BigDecimal vilandiCt,
			@RequestParam(value = "diamondsCt", required = false) BigDecimal diamondsCt,
			@RequestParam(value = "beadsCt", required = false) BigDecimal beadsCt,
			@RequestParam(value = "pearlsGm", required = false) BigDecimal pearlsGm,
			@RequestParam(value = "vilandiRate", required = false) BigDecimal vilandiRate,
			@RequestParam(value = "beadsRate", required = false) BigDecimal beadsRate,
			@RequestParam(value = "openPearlsRate", required = false) BigDecimal openPearlsRate,
			@RequestParam(value = "otherStonesCt", required = false) BigDecimal otherStonesCt,
			@RequestParam(value = "others", required = false) String others,
			@RequestParam(value = "designNo", required = false) String designNo,
			@RequestParam(value = "designNoEarrings", required = false) String designNoEarrings,
			@RequestParam(value = "pcsEarrings", required = false) Integer pcsEarrings,
			@RequestParam(value = "diamondsCtEarrings", required = false) BigDecimal diamondsCtEarrings,
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
			@RequestParam("image") MultipartFile imageFile) {
System.out.println("controller");
		Product updatedProduct = new Product();
	//	String design ="";
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
		//	design=designNo;
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
			updatedProduct.setBeadsCt(beadsCt);
			updatedProduct.setBdRate(beadsRate);
			updatedProduct.setPearlsGm(pearlsGm);
			updatedProduct.setPrlRate(openPearlsRate);
			updatedProduct.setStones(otherStonesCt);
			updatedProduct.setOthers(others);
			updatedProduct.setDesignNo(designNoOS);
		//	design=designNo;
		}
		if (categoryId == 3) {
			updatedProduct.setItem(productName);
			updatedProduct.setPrice(productPrice);
			updatedProduct.setRemarks(productRemarks);
			updatedProduct.setImageUrl(productImageUrl);
			updatedProduct.setCategoryId(categoryId);
			updatedProduct.setDesignNo(designNo);
			updatedProduct.setNet(productNet);
		//	design=designNo;
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
	//		design=designNoEarrings;
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
	//		design=designNoVilandi;
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
		//	design=designNoJadtar;
		}

		Product updated = productService.updateProduct(id, updatedProduct, imageFile);
		if (updated != null) {
			return ResponseEntity.ok(updated);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/getEstimate/{productId}")
	public ResponseEntity<Map<String, Object>> getEstimate(@PathVariable String productId) {
		Product product = productService.findProductById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
		List<Rate> rates = productService.getAllRates();
		BigDecimal goldPrice = null;
		BigDecimal diamondPrice = null;
		BigDecimal vilandiPrice = null;
		BigDecimal labour = null;
		BigDecimal gold1 = null;
		BigDecimal gst = null;
		BigDecimal estimate_gst = null;
		BigDecimal estimate_nogst = null;
		BigDecimal total = null;
		for (Rate rate : rates) {
			switch (rate.getCommodity().toLowerCase()) {
			case "gold":
				goldPrice = rate.getPrice();
				break;
			case "diamond":
				diamondPrice = rate.getPrice();
				break;
			case "labour":
				vilandiPrice = rate.getPrice();
				break;
			case "gst":
				gst = rate.getPrice();
				System.out.println("gst is set as " + gst);
				break;
			}
		}

		Map<String, Object> response = new HashMap<>();
		Estimate e = new Estimate();

		if (product.getCategoryId() == 6) {
			labour = product.getNet().multiply(BigDecimal.valueOf(1100));
			e.setLabour(labour);
			System.out.println(labour);
			gold1 = goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP);

			e.setGold(nullSafe(product.getNet()).multiply(gold1));
			System.out.println(e.getGold());
			e.setStones(nullSafe(product.getStones()).multiply(nullSafe(product.getStRate())));
			System.out.println(e.getStones());
			e.setBeads(nullSafe(product.getBeadsCt()).multiply(nullSafe(product.getBdRate())));
			System.out.println(e.getBeads());
			e.setPearls(nullSafe(product.getPearlsGm()).multiply(nullSafe(product.getPrlRate())));
			System.out.println(e.getPearls());
			e.setSsPearls(nullSafe(product.getSsPearlCt()).multiply(nullSafe(product.getSsRate())));
			System.out.println(e.getSsPearls());
			e.setMozo(nullSafe(product.getMozonite()).multiply(nullSafe(product.getmRate())));
			System.out.println(e.getMozo());
			e.setRealStones(nullSafe(product.getRealStone()));
			System.out.println(e.getRealStones());
			e.setFitting(nullSafe(product.getFitting()));
			System.out.println(e.getFitting());
			estimate_nogst = e.getGold().add(e.getLabour()).add(e.getStones()).add(e.getBeads()).add(e.getPearls())
					.add(e.getSsPearls()).add(e.getMozo()).add(e.getRealStones()).add(e.getFitting());
			System.out.println(estimate_nogst);
			e.setNogsttotal(estimate_nogst);
			System.out.println("gst value is " + gst);
			estimate_gst = estimate_nogst.multiply(gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
			e.setGst(estimate_gst);
			System.out.println(estimate_gst);
			total = estimate_nogst.add(estimate_gst);
			System.out.println("get estimate " + total);

			e.setTotal(total);
			response.put("object1", product);
			response.put("object2", e);
			response.put("object3", rates);
		}

		if (product.getCategoryId() == 5) {
			labour = product.getNet().multiply(BigDecimal.valueOf(1100));
			e.setLabour(labour);

			gold1 = goldPrice.divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP);

			e.setGold(nullSafe(product.getNet()).multiply(gold1));
			e.setStones(nullSafe(product.getStones()).multiply(nullSafe(product.getStRate())));
			e.setBeads(nullSafe(product.getBeadsCt()).multiply(nullSafe(product.getBdRate())));
			e.setPearls(nullSafe(product.getPearlsGm()).multiply(nullSafe(product.getPrlRate())));
			e.setSsPearls(nullSafe(product.getSsPearlCt()).multiply(nullSafe(product.getSsRate())));
			e.setVilandi(nullSafe(product.getVilandiCt()).multiply(nullSafe(product.getvRate())));
			e.setMozo(nullSafe(product.getMozonite()).multiply(nullSafe(product.getmRate())));
			e.setRealStones(nullSafe(product.getRealStone()));
			e.setFitting(nullSafe(product.getFitting()));

			estimate_nogst = e.getGold().add(e.getLabour()).add(e.getStones()).add(e.getBeads()).add(e.getPearls())
					.add(e.getSsPearls()).add(e.getVilandi()).add(e.getMozo()).add(e.getRealStones())
					.add(e.getFitting());
			System.out.println(estimate_nogst);
			estimate_gst = estimate_nogst.multiply(gst).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
			System.out.println(estimate_gst);
			e.setGst(estimate_gst);

			total = estimate_nogst.add(estimate_gst);
			System.out.println(total);
			e.setTotal(total);
			response.put("object1", product);
			response.put("object2", e);
			response.put("object3", rates);

		}

		System.out.println("reutn estimate");
		return ResponseEntity.ok(response);
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

	@GetMapping("/rates")
	@ResponseBody
	public List<Rate> getRates() {
		return productService.findAll();
	}

	private BigDecimal nullSafe(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}
}

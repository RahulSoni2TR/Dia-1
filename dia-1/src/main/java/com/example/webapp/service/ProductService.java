package com.example.webapp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.webapp.exceptions.ProductUpdateException;
import com.example.webapp.models.Orders;
import com.example.webapp.models.Product;
import com.example.webapp.models.Rate;
import com.example.webapp.repository.OrderRepository;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.RateRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private RateRepository rateRepository;
	

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private AmazonS3 s3Client;

	private final String bucketName = "elasticbeanstalk-ap-south-1-012676044441"; // Replace with your bucket name
	private final String region = "ap-south-1"; // Replace with your AWS region (e.g., "us-east-1")

	public Product findById(int a) {

		return new Product();
	}

	public void addProduct(Product product) {
		System.out.println("product values are " + product.getProductId() + " " + product.getItem());
		System.out.println("design no " + product.getDesignNo());
		Optional<Product> existingProduct = productRepository.findByDesignNo(product.getDesignNo());
		System.out.println("before inside here");
		if (existingProduct.isPresent()) {
			System.out.println("inside here");
			throw new IllegalArgumentException("Design number already exists");
		}
		System.out.println("outside here");
		productRepository.save(product);
	}

	@Transactional
	public void removeProduct(String productId) {
	    Optional<Product> productToDeleteOpt = productRepository.findByDesignNo(productId);

	    if (productToDeleteOpt.isPresent()) {
	        Product product = productToDeleteOpt.get();
	        Orders order = product.getOrders();

	        if (order != null) {
	            // Break bidirectional link
	            product.setOrders(null);             // Break reference in Product
	            order.setAssigned(false);            // Update flag
	            order.setAssignedProduct(null);      // Break reverse link in Order
	        }

	        // Save only Order, to persist changes
	        orderRepository.save(order);

	        // Now that order is detached, Hibernate won't try to cascade delete/insert it
	        productRepository.delete(product);
	    }
	}


	public String modifyProduct(int a, String s, String b, String n) {
		return "p";
	}

	public Page<Product> findAll(Pageable pageable) {
		return productRepository.findAll(pageable);
	}

	public List<Product> findProductsByCategoryId(Long categoryId) {
		return productRepository.findByCategoryId(categoryId);
	}

	public Optional<Product> findProductById(String productId) {
		return productRepository.findByDesignNo(productId);
	}

	public Optional<Product> findProductFromId(Long productId) {
		return productRepository.findById(productId);
	}
	
	public Optional<Orders> findByOrderId(String 	orderId){
		return orderRepository.findByOrderId(orderId);
	}
	
	public void saveOrders(Orders order){
		 orderRepository.save(order);
	}

	public Product updateProduct(String productId, Product updatedProduct, MultipartFile imageFile,String newOrderId) {
		Optional<Product> existingProductOpt = productRepository.findByDesignNo(productId);
		Optional<Product> alProduct = productRepository.findByDesignNo(updatedProduct.getDesignNo());
		if (alProduct.isPresent()) {
			Product alreadyProduct = alProduct.get();
			// Product existProduct = existingProductOpt.get();
			if (!alreadyProduct.getDesignNo().equals(productId)) {
				// System.out.println("came inside");
				throw new IllegalArgumentException("Design number already exists for a different product");
			}
		}
		if (existingProductOpt.isPresent()) {
			// System.out.println("product found");
			Product existingProduct = existingProductOpt.get();
			// Save the image if a new file isexistingProduct uploaded
			if (imageFile != null && !imageFile.isEmpty()) {
				String imageUrl = saveImageFile(imageFile);
				updatedProduct.setImageUrl(imageUrl);
			} else {
				// If no new image is uploaded, keep the existing image URL
				existingProduct.setImageUrl(existingProduct.getImageUrl());
			}
			
			
			 // 3. Handle order ID reassignment
		    Orders oldOrder = existingProduct.getOrders();

		    // Check if the order ID has changed
		    if (oldOrder == null || !oldOrder.getOrderId().equals(newOrderId)) {
		        // a) Unassign old order
		        if (oldOrder != null) {
		            oldOrder.setAssigned(false);
		            oldOrder.setAssignedProduct(null);
		            orderRepository.save(oldOrder);
		        }

		        // b) Handle new order
		        Orders newOrder = orderRepository.findByOrderId(newOrderId).orElseGet(() -> {
	                Orders o = new Orders();
	                o.setOrderId(newOrderId);
	                o.setCategoryId(updatedProduct.getCategoryId());
	                return o;
	            });


		        // Prevent re-assigning if already taken
		        if (newOrder.isAssigned() && (newOrder.getAssignedProduct() == null ||
		                !newOrder.getAssignedProduct().getProductId().equals(existingProduct.getProductId()))) {
		            throw new ProductUpdateException("New Order ID is already assigned to another product.");
		        }

		        newOrder.setAssigned(true);
		        newOrder.setAssignedProduct(existingProduct);
		        orderRepository.save(newOrder);

		        // Update product's orderRef
		        existingProduct.setOrders(newOrder);
		    }

			if (updatedProduct.getCategoryId() == 1) {
				existingProduct.setItem(updatedProduct.getItem());
				existingProduct.setPrice(updatedProduct.getPrice());
				existingProduct.setRemarks(updatedProduct.getRemarks());
				existingProduct.setImageUrl(updatedProduct.getImageUrl());
				existingProduct.setCategoryId(updatedProduct.getCategoryId());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setPcs(updatedProduct.getPcs());
				existingProduct.setDiaWeight(updatedProduct.getDiaWeight());
				existingProduct.setLabour(updatedProduct.getLabour());
				existingProduct.setLabourAll(updatedProduct.getLabourAll());
				existingProduct.setKarat(updatedProduct.getKarat());
			}
			if (updatedProduct.getCategoryId() == 2) {
				existingProduct.setItem(updatedProduct.getItem());
				existingProduct.setPrice(updatedProduct.getPrice());
				existingProduct.setRemarks(updatedProduct.getRemarks());
				existingProduct.setImageUrl(updatedProduct.getImageUrl());
				existingProduct.setCategoryId(updatedProduct.getCategoryId());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setGross(updatedProduct.getGross());
				existingProduct.setVilandiCt(updatedProduct.getVilandiCt());
				existingProduct.setvRate(updatedProduct.getvRate());
				existingProduct.setDiamondsCt(updatedProduct.getDiamondsCt());
				existingProduct.setBeadsCt(updatedProduct.getBeadsCt());
				existingProduct.setBdRate(updatedProduct.getBdRate());
				existingProduct.setPearlsGm(updatedProduct.getPearlsGm());
				existingProduct.setPrlRate(updatedProduct.getPrlRate());
				existingProduct.setStones(updatedProduct.getStones());
				existingProduct.setOthers(updatedProduct.getOthers());
				existingProduct.setLabour(updatedProduct.getLabour());
				existingProduct.setLabourAll(updatedProduct.getLabourAll());
				existingProduct.setKarat(updatedProduct.getKarat());
			}
			if (updatedProduct.getCategoryId() == 3) {
				existingProduct.setItem(updatedProduct.getItem());
				existingProduct.setPrice(updatedProduct.getPrice());
				existingProduct.setRemarks(updatedProduct.getRemarks());
				existingProduct.setImageUrl(updatedProduct.getImageUrl());
				existingProduct.setCategoryId(updatedProduct.getCategoryId());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setDesignNo(updatedProduct.getDesignNo());
				existingProduct.setLabour(updatedProduct.getLabour());
				existingProduct.setLabourAll(updatedProduct.getLabourAll());
				existingProduct.setKarat(updatedProduct.getKarat());
			}
			if (updatedProduct.getCategoryId() == 4) {
				existingProduct.setItem(updatedProduct.getItem());
				existingProduct.setPrice(updatedProduct.getPrice());
				existingProduct.setRemarks(updatedProduct.getRemarks());
				existingProduct.setImageUrl(updatedProduct.getImageUrl());
				existingProduct.setCategoryId(updatedProduct.getCategoryId());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setDesignNo(updatedProduct.getDesignNo());
				existingProduct.setPcs(updatedProduct.getPcs());
				existingProduct.setDiamondsCt(updatedProduct.getDiamondsCt());
				existingProduct.setLabour(updatedProduct.getLabour());
				existingProduct.setLabourAll(updatedProduct.getLabourAll());
				existingProduct.setKarat(updatedProduct.getKarat());
			}
			if (updatedProduct.getCategoryId() == 5) {
				existingProduct.setItem(updatedProduct.getItem());
				existingProduct.setPrice(updatedProduct.getPrice());
				existingProduct.setRemarks(updatedProduct.getRemarks());
				existingProduct.setImageUrl(updatedProduct.getImageUrl());
				existingProduct.setCategoryId(updatedProduct.getCategoryId());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setDesignNo(updatedProduct.getDesignNo());
				existingProduct.setGross(updatedProduct.getGross());
				existingProduct.setVilandiCt(updatedProduct.getVilandiCt());
				existingProduct.setvRate(updatedProduct.getvRate());
				existingProduct.setStones(updatedProduct.getStones());
				existingProduct.setStRate(updatedProduct.getStRate());
				existingProduct.setBeadsCt(updatedProduct.getBeadsCt());
				existingProduct.setBdRate(updatedProduct.getBdRate());
				existingProduct.setPearlsGm(updatedProduct.getPearlsGm());
				existingProduct.setPrlRate(updatedProduct.getPrlRate());
				existingProduct.setSsPearlCt(updatedProduct.getSsPearlCt());
				existingProduct.setSsRate(updatedProduct.getSsRate());
				existingProduct.setRealStone(updatedProduct.getRealStone());
				existingProduct.setFitting(updatedProduct.getFitting());
				existingProduct.setMozonite(updatedProduct.getMozonite());
				existingProduct.setmRate(updatedProduct.getmRate());
				existingProduct.setLabour(updatedProduct.getLabour());
				existingProduct.setLabourAll(updatedProduct.getLabourAll());
				existingProduct.setKarat(updatedProduct.getKarat());
			}
			if (updatedProduct.getCategoryId() == 6) {
				existingProduct.setItem(updatedProduct.getItem());
				existingProduct.setPrice(updatedProduct.getPrice());
				existingProduct.setRemarks(updatedProduct.getRemarks());
				existingProduct.setImageUrl(updatedProduct.getImageUrl());
				existingProduct.setCategoryId(updatedProduct.getCategoryId());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setDesignNo(updatedProduct.getDesignNo());
				existingProduct.setGross(updatedProduct.getGross());
				existingProduct.setNet(updatedProduct.getNet());
				existingProduct.setStones(updatedProduct.getStones());
				existingProduct.setStRate(updatedProduct.getStRate());
				existingProduct.setBeadsCt(updatedProduct.getBeadsCt());
				existingProduct.setBdRate(updatedProduct.getBdRate());
				existingProduct.setPearlsGm(updatedProduct.getPearlsGm());
				existingProduct.setPrlRate(updatedProduct.getPrlRate());
				existingProduct.setSsPearlCt(updatedProduct.getSsPearlCt());
				existingProduct.setSsRate(updatedProduct.getSsRate());
				existingProduct.setRealStone(updatedProduct.getRealStone());
				existingProduct.setFitting(updatedProduct.getFitting());
				existingProduct.setMozonite(updatedProduct.getMozonite());
				existingProduct.setmRate(updatedProduct.getmRate());
				existingProduct.setLabour(updatedProduct.getLabour());
				existingProduct.setLabourAll(updatedProduct.getLabourAll());
				existingProduct.setKarat(updatedProduct.getKarat());
			}
			return productRepository.save(existingProduct);
		}
		return null; // Or throw an exception as appropriate
	}

	public static ModelMapper getConfiguredModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true); // Skip
																												// null
																												// values
		return modelMapper;
	}

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

	public Page<Product> findByCategory(Integer category, Pageable pageable, String searchTerm) {
		return productRepository.findProductsByCategoryAndName(searchTerm, category, pageable);
	}

	public Page<Product> findWithoutCategory(Integer category, Pageable pageable, String searchTerm) {
		System.out.println(searchTerm);
		if (searchTerm != null && !searchTerm.isEmpty()) {
			searchTerm = "%" + searchTerm + "%"; // Add wildcards
		} else {
			searchTerm = null; // Ensure NULL is passed when searchTerm is empty
		}
		return productRepository.findProductsWithoutCategoryAndName(searchTerm, pageable);
	}

	public List<Rate> getAllRates() {
		return rateRepository.findAll();
	}

	public String updatePrices(Map<String, BigDecimal> prices) {
		prices.forEach((commodity, price) -> {
			if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
				Rate rate = rateRepository.findByCommodity(commodity).orElse(new Rate()); // Create new Rate if
																							// commodity doesn't exist
				rate.setCommodity(commodity);
				rate.setPrice(price);
				rateRepository.save(rate);
			} else {
				// Handle invalid price (optional logging or feedback)
				System.out.println("Invalid price for commodity: " + commodity);
			}
		});
		return "Prices updated successfully";
	}

	public List<Rate> findAll() {
		return rateRepository.findAll();
	}

	public Page<Product> getFilteredProducts(Long categoryId, LocalDateTime startDate, LocalDateTime endDate, int page,
			int size) {
		Pageable pageable = PageRequest.of(page, size);
		// System.out.println(startDate);
		// System.out.println(endDate);
		return productRepository.findProductsByCategoryAndDateRange(categoryId, startDate, endDate, pageable);
	}
	
	public List<Product> getAllProductsByCategory(Long categoryId) {
	    return productRepository.findByCategoryId(categoryId);
	}


	public void importProductsFromExcel(MultipartFile file, String categoryName) throws Exception {
		System.out.println("inside import product");
		// Set<String> uniqueDesignNos = new HashSet<>();
		Workbook workbook = null;
		String category = "";
		try {
			InputStream inputStream = file.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			if (categoryName.equals("1")) {
				category = "Diamond Rings";
			}
			if (categoryName.equals("2")) {
				category = "Open Setting";
			}
			if (categoryName.equals("3")) {
				category = "Chains";
			}
			if (categoryName.equals("4")) {
				category = "Diamond Earrings";
			}
			if (categoryName.equals("5")) {
				category = "Vilandi";
			}
			if (categoryName.equals("6")) {
				category = "Jadtar_Register";
			}

			Sheet sheet = workbook.getSheet(category);

			if (sheet == null) {
				throw new IllegalArgumentException("Sheet with name " + categoryName + " not found");
			}

			List<Product> products = new ArrayList<>();
			// System.out.println("before sheet iterate");
			Iterator<Row> rows = sheet.iterator();
			// System.out.println("after sheet iterate");
			int rowNumber = 0;

			if (category.equals("Diamond Rings")) {
				while (rows.hasNext()) {
					System.out.println("inside rows");
					Row currentRow = rows.next();

					// Skip the header row
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					try {
						Product product = new Product();
						String designNo = getCellValue(currentRow.getCell(1));

						// Check if the designNo is unique
						if (designNo != null && !designNo.trim().isEmpty()) {
							while (productRepository.existsByDesignNo(designNo)) { // Add method to check uniqueness
								designNo = generateRandomDesignNo();
							}
						} else {
							// Generate a random design number if none is provided
							designNo = generateRandomDesignNo();
						}
						String orderId = getCellValue(currentRow.getCell(2));
						Optional<Orders> orderOpt = orderRepository.findByOrderId(orderId);
						Orders order;
						if (orderOpt.isPresent()) {
						    Orders existingOrder = orderOpt.get();
						    
						    if (existingOrder.isAssigned()) {
						        // Existing order is already assigned, generate a new unique one
						        String newOrderId = generateUniqueOrderIdForCategory(1);
						        
						        order = new Orders();
						        order.setOrderId(newOrderId);
						        order.setCategoryId(1);
						        order.setAssigned(true);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						       
						    } else {
						        // Existing order is not assigned, reuse it
						        order = existingOrder;
						        order.setAssigned(true);
						        order.setCategoryId(1);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						        
						    }
						} else {
						    // Create a fresh order with the provided ID
						    order = new Orders();
						    order.setOrderId(orderId);
						    order.setCategoryId(1);
						    order.setAssigned(true);
						    order.setAssignedProduct(product);
						    product.setOrders(order);
						}

						
//						product.setOrderRef(order);
						product.setCategoryId(parseInt(categoryName));
						product.setImageUrl(
								"https://elasticbeanstalk-ap-south-1-012676044441.s3.ap-south-1.amazonaws.com/unavailable.jpg");
						product.setDesignNo(designNo);
						product.setItem(getCellValue(currentRow.getCell(3)));
						product.setNet(parseBigDecimal(getCellValue(currentRow.getCell(4))));
						product.setPcs(parseInt(getCellValue(currentRow.getCell(5))));
						product.setDiamondsCt(parseBigDecimal(getCellValue(currentRow.getCell(6))));
						product.setRemarks(getCellValue(currentRow.getCell(7)));
						product.setLabour(parseBigDecimal(getCellValue(currentRow.getCell(8))));
						product.setLabourAll(parseBigDecimal(getCellValue(currentRow.getCell(9))));
						product.setKarat(parseBigDecimal(getCellValue(currentRow.getCell(10))));
					//	order.setAssignedProduct(product);
						System.out.println("product detail " + product);
						products.add(product);
					} catch (Exception e) {
						System.err.println("Error parsing row number " + rowNumber + ": " + e.getMessage());
						e.printStackTrace(); // Log stack trace for debugging
					}

					rowNumber++;
				}

			}

			if (category.equals("Open Setting")) {
				while (rows.hasNext()) {
					System.out.println("inside rows");
					Row currentRow = rows.next();

					// Skip the header row
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					try {
						Product product = new Product();
						String designNo = getCellValue(currentRow.getCell(1));

						// Check if the designNo is unique
						if (designNo != null && !designNo.trim().isEmpty()) {
							while (productRepository.existsByDesignNo(designNo)) { // Add method to check uniqueness
								designNo = generateRandomDesignNo();
							}
						} else {
							// Generate a random design number if none is provided
							designNo = generateRandomDesignNo();
						}
						String orderId = getCellValue(currentRow.getCell(2));
						Optional<Orders> orderOpt = orderRepository.findByOrderId(orderId);
						Orders order;
						if (orderOpt.isPresent()) {
						    Orders existingOrder = orderOpt.get();
						    
						    if (existingOrder.isAssigned()) {
						        // Existing order is already assigned, generate a new unique one
						        String newOrderId = generateUniqueOrderIdForCategory(2);
						        
						        order = new Orders();
						        order.setOrderId(newOrderId);
						        order.setCategoryId(2);
						        order.setAssigned(true);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						       
						    } else {
						        // Existing order is not assigned, reuse it
						        order = existingOrder;
						        order.setAssigned(true);
						        order.setCategoryId(2);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						        
						    }
						} else {
						    // Create a fresh order with the provided ID
						    order = new Orders();
						    order.setOrderId(orderId);
						    order.setCategoryId(2);
						    order.setAssigned(true);
						    order.setAssignedProduct(product);
						    product.setOrders(order);
						}

						
//						product.setOrderRef(order);
						product.setDesignNo(designNo);
						product.setImageUrl(
								"https://elasticbeanstalk-ap-south-1-012676044441.s3.ap-south-1.amazonaws.com/unavailable.jpg");
						product.setCategoryId(parseInt(categoryName));
						product.setItem(getCellValue(currentRow.getCell(3)));
						product.setGross(parseBigDecimal(getCellValue(currentRow.getCell(4))));
						product.setNet(parseBigDecimal(getCellValue(currentRow.getCell(5))));
						product.setVilandiCt(parseBigDecimal(getCellValue(currentRow.getCell(6))));
						product.setDiamondsCt(parseBigDecimal(getCellValue(currentRow.getCell(7))));
						product.setBeadsCt(parseBigDecimal(getCellValue(currentRow.getCell(8))));
						product.setPearlsGm(parseBigDecimal(getCellValue(currentRow.getCell(9))));
						product.setOtherStonesCt(parseBigDecimal(getCellValue(currentRow.getCell(10))));
						product.setRemarks(getCellValue(currentRow.getCell(11)));
						product.setLabour(parseBigDecimal(getCellValue(currentRow.getCell(12))));
						product.setLabourAll(parseBigDecimal(getCellValue(currentRow.getCell(13))));
						product.setKarat(parseBigDecimal(getCellValue(currentRow.getCell(14))));

						System.out.println("product detail " + product);
					//	order.setAssignedProduct(product);
						products.add(product);

					} catch (Exception e) {
						System.err.println("Error parsing row number " + rowNumber + ": " + e.getMessage());
						e.printStackTrace(); // Log stack trace for debugging
					}

					rowNumber++;
				}

			}

			if (category.equals("Chains")) {
				while (rows.hasNext()) {
					System.out.println("inside rows");
					Row currentRow = rows.next();

					// Skip the header row
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					try {
						Product product = new Product();
						String designNo = getCellValue(currentRow.getCell(1));

						// Check if the designNo is unique
						if (designNo != null && !designNo.trim().isEmpty()) {
							while (productRepository.existsByDesignNo(designNo)) { // Add method to check uniqueness
								designNo = generateRandomDesignNo();
							}
						} else {
							// Generate a random design number if none is provided
							designNo = generateRandomDesignNo();
						}
						String orderId = getCellValue(currentRow.getCell(2));
						Optional<Orders> orderOpt = orderRepository.findByOrderId(orderId);
						Orders order;
						if (orderOpt.isPresent()) {
						    Orders existingOrder = orderOpt.get();
						    
						    if (existingOrder.isAssigned()) {
						        // Existing order is already assigned, generate a new unique one
						        String newOrderId = generateUniqueOrderIdForCategory(3);
						        
						        order = new Orders();
						        order.setOrderId(newOrderId);
						        order.setCategoryId(3);
						        order.setAssigned(true);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						       
						    } else {
						        // Existing order is not assigned, reuse it
						        order = existingOrder;
						        order.setAssigned(true);
						        order.setCategoryId(3);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						        
						    }
						} else {
						    // Create a fresh order with the provided ID
						    order = new Orders();
						    order.setOrderId(orderId);
						    order.setCategoryId(3);
						    order.setAssigned(true);
						    order.setAssignedProduct(product);
						    product.setOrders(order);
						}

					//	order.setAssignedProduct(product);
//						product.setOrderRef(order);
						product.setDesignNo(designNo);
						product.setImageUrl(
								"https://elasticbeanstalk-ap-south-1-012676044441.s3.ap-south-1.amazonaws.com/unavailable.jpg");
						product.setCategoryId(parseInt(categoryName));
						product.setItem(getCellValue(currentRow.getCell(3)));
						product.setNet(parseBigDecimal(getCellValue(currentRow.getCell(4))));
						product.setLabour(parseBigDecimal(getCellValue(currentRow.getCell(5))));
						product.setLabourAll(parseBigDecimal(getCellValue(currentRow.getCell(6))));
						product.setKarat(parseBigDecimal(getCellValue(currentRow.getCell(7))));
						product.setRemarks(getCellValue(currentRow.getCell(8)));
				//		order.setAssignedProduct(product);
						System.out.println("product detail " + product);
						products.add(product);
					} catch (Exception e) {
						System.err.println("Error parsing row number " + rowNumber + ": " + e.getMessage());
						e.printStackTrace(); // Log stack trace for debugging
					}

					rowNumber++;
				}

			}

			if (category.equals("Diamond Earrings")) {
				while (rows.hasNext()) {
					System.out.println("inside rows");
					Row currentRow = rows.next();

					// Skip the header row
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					try {
						Product product = new Product();
						String designNo = getCellValue(currentRow.getCell(1));

						// Check if the designNo is unique
						if (designNo != null && !designNo.trim().isEmpty()) {
							while (productRepository.existsByDesignNo(designNo)) { // Add method to check uniqueness
								designNo = generateRandomDesignNo();
							}
						} else {
							// Generate a random design number if none is provided
							designNo = generateRandomDesignNo();
						}
						String orderId = getCellValue(currentRow.getCell(2));
						Optional<Orders> orderOpt = orderRepository.findByOrderId(orderId);
						Orders order;
						if (orderOpt.isPresent()) {
						    Orders existingOrder = orderOpt.get();
						    
						    if (existingOrder.isAssigned()) {
						        // Existing order is already assigned, generate a new unique one
						        String newOrderId = generateUniqueOrderIdForCategory(4);
						        
						        order = new Orders();
						        order.setOrderId(newOrderId);
						        order.setCategoryId(4);
						        order.setAssigned(true);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						       
						    } else {
						        // Existing order is not assigned, reuse it
						        order = existingOrder;
						        order.setAssigned(true);
						        order.setCategoryId(4);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						        
						    }
						} else {
						    // Create a fresh order with the provided ID
						    order = new Orders();
						    order.setOrderId(orderId);
						    order.setCategoryId(4);
						    order.setAssigned(true);
						    order.setAssignedProduct(product);
						    product.setOrders(order);
						}
						
					//	product.setOrders(order);
						product.setDesignNo(designNo);
						product.setImageUrl(
								"https://elasticbeanstalk-ap-south-1-012676044441.s3.ap-south-1.amazonaws.com/unavailable.jpg");
						product.setCategoryId(parseInt(categoryName));
						product.setItem(getCellValue(currentRow.getCell(3)));
						product.setNet(parseBigDecimal(getCellValue(currentRow.getCell(4))));
						product.setPcs(parseInt(getCellValue(currentRow.getCell(5))));
						product.setDiamondsCt(parseBigDecimal(getCellValue(currentRow.getCell(6))));
						product.setRemarks(getCellValue(currentRow.getCell(7)));
						product.setLabour(parseBigDecimal(getCellValue(currentRow.getCell(8))));
						product.setLabourAll(parseBigDecimal(getCellValue(currentRow.getCell(9))));
						product.setKarat(parseBigDecimal(getCellValue(currentRow.getCell(10))));
				//		order.setAssignedProduct(product);
						System.out.println("product detail " + product);
						products.add(product);
					} catch (Exception e) {
						System.err.println("Error parsing row number " + rowNumber + ": " + e.getMessage());
						e.printStackTrace(); // Log stack trace for debugging
					}

					rowNumber++;
				}

			}

			if (category.equals("Vilandi")) {
				while (rows.hasNext()) {
					System.out.println("inside rows");
					Row currentRow = rows.next();

					// Skip the header row
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					try {
						Product product = new Product();
						// product.setId(parseLong(getCellValue(currentRow.getCell(0)))); // Uncomment
						// if needed
						String designNo = getCellValue(currentRow.getCell(1));

						// Check if the designNo is unique
						if (designNo != null && !designNo.trim().isEmpty()) {
							while (productRepository.existsByDesignNo(designNo)) {
								System.out.println("inside design validation before " + designNo);// Add method to check
																									// uniqueness
								designNo = generateRandomDesignNo();
								System.out.println("inside design validation after " + designNo);
							}
						} else {
							// Generate a random design number if none is provided
							System.out.println("inside design validation else");
							designNo = generateRandomDesignNo();
						}
						String orderId = getCellValue(currentRow.getCell(2));
						Optional<Orders> orderOpt = orderRepository.findByOrderId(orderId);
						Orders order;
						if (orderOpt.isPresent()) {
						    Orders existingOrder = orderOpt.get();
						    
						    if (existingOrder.isAssigned()) {
						        // Existing order is already assigned, generate a new unique one
						        String newOrderId = generateUniqueOrderIdForCategory(5);
						        
						        order = new Orders();
						        order.setOrderId(newOrderId);
						        order.setCategoryId(5);
						        order.setAssigned(true);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						       
						    } else {
						        // Existing order is not assigned, reuse it
						        order = existingOrder;
						        order.setAssigned(true);
						        order.setCategoryId(5);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						        
						    }
						} else {
						    // Create a fresh order with the provided ID
						    order = new Orders();
						    order.setOrderId(orderId);
						    order.setCategoryId(5);
						    order.setAssigned(true);
						    order.setAssignedProduct(product);
						    product.setOrders(order);
						}
						product.setDesignNo(designNo);
						product.setImageUrl(
								"https://elasticbeanstalk-ap-south-1-012676044441.s3.ap-south-1.amazonaws.com/unavailable.jpg");
						product.setCategoryId(parseInt(categoryName));
						product.setItem(getCellValue(currentRow.getCell(3)));
						product.setVilandiCt(parseBigDecimal(getCellValue(currentRow.getCell(4))));
						product.setvRate(parseBigDecimal(getCellValue(currentRow.getCell(5))));
						product.setGross(parseBigDecimal(getCellValue(currentRow.getCell(6))));
						product.setNet(parseBigDecimal(getCellValue(currentRow.getCell(7))));
						product.setStones(parseBigDecimal(getCellValue(currentRow.getCell(8))));
						product.setBeadsCt(parseBigDecimal(getCellValue(currentRow.getCell(9))));
						product.setBdRate(parseBigDecimal(getCellValue(currentRow.getCell(10))));
						product.setPearlsGm(parseBigDecimal(getCellValue(currentRow.getCell(11))));
						product.setPrlRate(parseBigDecimal(getCellValue(currentRow.getCell(12))));
						product.setSsPearlCt(parseBigDecimal(getCellValue(currentRow.getCell(13))));
						product.setSsRate(parseBigDecimal(getCellValue(currentRow.getCell(14))));
						product.setRealStone(parseBigDecimal(getCellValue(currentRow.getCell(15))));
						product.setFitting(parseBigDecimal(getCellValue(currentRow.getCell(16))));
						product.setMozonite(parseBigDecimal(getCellValue(currentRow.getCell(17))));
						product.setmRate(parseBigDecimal(getCellValue(currentRow.getCell(18))));
						product.setLabour(parseBigDecimal(getCellValue(currentRow.getCell(19))));
						product.setLabourAll(parseBigDecimal(getCellValue(currentRow.getCell(20))));
						product.setKarat(parseBigDecimal(getCellValue(currentRow.getCell(21))));
						product.setRemarks(getCellValue(currentRow.getCell(22)));
				//		order.setAssignedProduct(product);

						System.out.println("product detail " + product);
						products.add(product);
					} catch (Exception e) {
						System.err.println("Error parsing row number " + rowNumber + ": " + e.getMessage());
						e.printStackTrace(); // Log stack trace for debugging
					}

					rowNumber++;
				}

			}

			if (category.equals("Jadtar_Register")) {
				while (rows.hasNext()) {
					System.out.println("inside rows");
					Row currentRow = rows.next();

					// Skip the header row
					if (rowNumber == 0) {
						rowNumber++;
						continue;
					}

					try {
						Product product = new Product();
						// product.setId(parseLong(getCellValue(currentRow.getCell(0)))); // Uncomment
						// if needed
						String designNo = getCellValue(currentRow.getCell(1));

						// Check if the designNo is unique
						if (designNo != null && !designNo.trim().isEmpty()) {
							while (productRepository.existsByDesignNo(designNo)) { // Add method to check uniqueness
								designNo = generateRandomDesignNo();
							}
						} else {
							// Generate a random design number if none is provided
							designNo = generateRandomDesignNo();
						}
						String orderId = getCellValue(currentRow.getCell(2));
						Optional<Orders> orderOpt = orderRepository.findByOrderId(orderId);
						Orders order;
						if (orderOpt.isPresent()) {
						    Orders existingOrder = orderOpt.get();
						    
						    if (existingOrder.isAssigned()) {
						        // Existing order is already assigned, generate a new unique one
						        String newOrderId = generateUniqueOrderIdForCategory(6);
						        
						        order = new Orders();
						        order.setOrderId(newOrderId);
						        order.setCategoryId(6);
						        order.setAssigned(true);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						       
						    } else {
						        // Existing order is not assigned, reuse it
						        order = existingOrder;
						        order.setAssigned(true);
						        order.setCategoryId(6);
						        order.setAssignedProduct(product);
						        product.setOrders(order);
						        
						    }
						} else {
						    // Create a fresh order with the provided ID
						    order = new Orders();
						    order.setOrderId(orderId);
						    order.setCategoryId(6);
						    order.setAssigned(true);
						    order.setAssignedProduct(product);
						    product.setOrders(order);
						}
						product.setDesignNo(designNo);
						product.setImageUrl(
								"https://elasticbeanstalk-ap-south-1-012676044441.s3.ap-south-1.amazonaws.com/unavailable.jpg");
						product.setCategoryId(parseInt(categoryName));
						product.setItem(getCellValue(currentRow.getCell(3)));
						product.setGross(parseBigDecimal(getCellValue(currentRow.getCell(4))));
						product.setNet(parseBigDecimal(getCellValue(currentRow.getCell(5))));
						product.setStones(parseBigDecimal(getCellValue(currentRow.getCell(6))));
						product.setStRate(parseBigDecimal(getCellValue(currentRow.getCell(7))));
						product.setBeadsCt(parseBigDecimal(getCellValue(currentRow.getCell(8))));
						product.setBdRate(parseBigDecimal(getCellValue(currentRow.getCell(9))));
						product.setPearlsGm(parseBigDecimal(getCellValue(currentRow.getCell(10))));
						product.setPrlRate(parseBigDecimal(getCellValue(currentRow.getCell(11))));
						product.setSsPearlCt(parseBigDecimal(getCellValue(currentRow.getCell(12))));
						product.setSsRate(parseBigDecimal(getCellValue(currentRow.getCell(13))));
						product.setRealStone(parseBigDecimal(getCellValue(currentRow.getCell(14))));
						product.setFitting(parseBigDecimal(getCellValue(currentRow.getCell(15))));
						product.setMozonite(parseBigDecimal(getCellValue(currentRow.getCell(16))));
						product.setmRate(parseBigDecimal(getCellValue(currentRow.getCell(17))));
						product.setLabour(parseBigDecimal(getCellValue(currentRow.getCell(18))));
						product.setLabourAll(parseBigDecimal(getCellValue(currentRow.getCell(19))));
						product.setKarat(parseBigDecimal(getCellValue(currentRow.getCell(20))));
						product.setRemarks(getCellValue(currentRow.getCell(21)));
						System.out.println("product detail " + product);
				//		order.setAssignedProduct(product);
						products.add(product);
					} catch (Exception e) {
						System.err.println("Error parsing row number " + rowNumber + ": " + e.getMessage());
						e.printStackTrace(); // Log stack trace for debugging
					}

					rowNumber++;
				}

			}
			saveProducts(products);
			// productRepository.saveAll(products);

		} catch (Exception e) {
			System.err.println("Exception occurred while processing Excel file: " + e.getMessage());
			e.printStackTrace(); // Log stack trace for debugging
			throw new Exception("Error while processing the Excel file: " + e.getMessage());
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}
	}

	public void saveProducts(List<Product> products) {
		Set<String> uniqueDesignNos = new HashSet<>();

		for (Product product : products) {
			String designNo = product.getDesignNo();

			// If designNo already exists, generate a new unique one
			while (uniqueDesignNos.contains(designNo)) {
				designNo = generateRandomDesignNo();
				product.setDesignNo(designNo);
			}

			// Add the designNo to the set to track it
			uniqueDesignNos.add(designNo);
		}

		// Save updated product list
		productRepository.saveAll(products);
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return null;
		}
	}

	private Double parseDouble(String value) {
		try {
			return value != null ? Double.parseDouble(value) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseInt(String value) {
		try {
			return value != null ? Integer.parseInt(value) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Long parseLong(String value) {
		try {
			return value != null ? Long.parseLong(value) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String value) {
		try {
			return value == null || value.trim().isEmpty() ? null : new BigDecimal(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid numeric value: " + value);
		}
	}

	private String generateRandomDesignNo() {
		return "DESIGN-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
	}

	
	public List<Orders> findByCategoryIdAndIsAssignedFalse(Long categoryId){
		
		return orderRepository.findByCategoryIdAndIsAssignedFalse(categoryId);
	}
	
	public String generateUniqueOrderIdForCategory(Integer categoryId) {
	    String prefix = "";

	    switch (categoryId) {
	        case 1: prefix = "dia"; break;
	        case 2: prefix = "ope"; break;
	        case 3: prefix = "cha"; break;
	        case 4: prefix = "ear"; break;
	        case 5: prefix = "vil"; break;
	        case 6: prefix = "jad"; break;
	        default: prefix = "ord"; break;
	    }

	    String newOrderId;
	    do {
	        int randomNum = (int)(Math.random() * 10000); // adjust range if needed
	        newOrderId = prefix + randomNum;
	    } while (orderRepository.existsByOrderId(newOrderId));

	    return newOrderId;
	}


}

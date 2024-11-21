package com.example.webapp.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.webapp.models.Product;
import com.example.webapp.models.Rate;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.RateRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
    private RateRepository rateRepository;
	
	public Product findById(int a) {
		
		return new Product();
	}
	
	public void addProduct(Product product) {
		System.out.println("product values are "+product.getProductId()+" "+product.getItem());
        productRepository.save(product);
    }
	
	public void removeProduct(Long productId) {
		        productRepository.deleteById(productId);
	}

	
	public String modifyProduct(int a,String s,String b, String n) {
		return "p";
	}
	
	public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
	
	public List<Product> findProductsByCategoryId(Long categoryId) {
	        return productRepository.findByCategoryId(categoryId);
    }

	public Optional<Product> findProductById(Long productId) {
	        return productRepository.findById(productId);
    }
	
	public Product updateProduct(Long productId, Product updatedProduct, MultipartFile imageFile) {
	    Optional<Product> existingProductOpt = productRepository.findById(productId);
	    if (existingProductOpt.isPresent()) {
	        Product existingProduct = existingProductOpt.get();

	        // Update common fields
	        existingProduct.setItem(updatedProduct.getItem());
	        existingProduct.setNet(updatedProduct.getNet());
	        existingProduct.setRemarks(updatedProduct.getRemarks());

	        // Save the image if a new file is uploaded
	        if (imageFile != null && !imageFile.isEmpty()) {
	            String imageUrl = saveImageFile(imageFile);
	            existingProduct.setImageUrl(imageUrl);
	        } else {
	            // If no new image is uploaded, keep the existing image URL
	            existingProduct.setImageUrl(existingProduct.getImageUrl());
	        }

	        // Update fields based on category
	        switch (updatedProduct.getCategoryId().intValue()) {
	            case 1: // Diamond Rings
	                existingProduct.setPcs(updatedProduct.getPcs());
	                existingProduct.setDiaWeight(updatedProduct.getDiaWeight());          
	                break;
	            case 2: // Open Setting
	                existingProduct.setGross(updatedProduct.getGross());
	                existingProduct.setVilandiCt(updatedProduct.getVilandiCt());
	                existingProduct.setDiamondsCt(updatedProduct.getDiamondsCt());
	                existingProduct.setBeadsCt(updatedProduct.getBeadsCt());
	                existingProduct.setPearlsGm(updatedProduct.getPearlsGm());
	                existingProduct.setOtherStonesCt(updatedProduct.getOtherStonesCt());
	                existingProduct.setOthers(updatedProduct.getOthers());
	                break;
	            case 3: // Chains
	                existingProduct.setDesignNo(updatedProduct.getDesignNo());
	                break;
	            case 4: // Diamond Earrings
	                existingProduct.setDesignNo(updatedProduct.getDesignNo());
	                existingProduct.setPcs(updatedProduct.getPcs());
	                existingProduct.setDiamondsCt(updatedProduct.getDiamondsCt());
	                break;
	            case 5: // Vilandi
	                existingProduct.setDesignNo(updatedProduct.getDesignNo());
	                existingProduct.setVilandiCt(updatedProduct.getVilandiCt());
	                existingProduct.setGross(updatedProduct.getGross());
	                existingProduct.setNet(updatedProduct.getNet());
	                existingProduct.setStones(updatedProduct.getStones());
	                existingProduct.setBeadsCt(updatedProduct.getBeadsCt());
	                existingProduct.setPearlsGm(updatedProduct.getPearlsGm());
	                existingProduct.setSsPearlCt(updatedProduct.getSsPearlCt());
	                existingProduct.setRealStone(updatedProduct.getRealStone());
	                break;
	            case 6: // Jadtar Register
	                existingProduct.setDesignNo(updatedProduct.getDesignNo());
	                existingProduct.setGross(updatedProduct.getGross());
	                existingProduct.setNet(updatedProduct.getNet());
	                existingProduct.setStones(updatedProduct.getStones());
	                existingProduct.setBeadsCt(updatedProduct.getBeadsCt());
	                existingProduct.setPearlsGm(updatedProduct.getPearlsGm());
	                existingProduct.setSsPearlCt(updatedProduct.getSsPearlCt());
	                existingProduct.setRealStone(updatedProduct.getRealStone());
	                break;
	            default:
	                throw new IllegalArgumentException("Unknown category ID: " + updatedProduct.getCategoryId());
	        }

	        return productRepository.save(existingProduct);
	    }
	    return null; // Or throw an exception as appropriate
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

	  public Page<Product> findByCategory(Integer category, Pageable pageable) {
	        return productRepository.findByCategoryId(category, pageable);
	    }
	  
	  public List<Rate> getAllRates() {
	        return rateRepository.findAll();
	    }

	  public String updatePrices(Map<String, BigDecimal> prices) {
	        prices.forEach((commodity, price) -> {
	            Rate rate = rateRepository.findByCommodity(commodity)
	                                       .orElse(new Rate());  // Create new Rate if commodity doesn't exist
	            rate.setCommodity(commodity);
	            rate.setPrice(price);
	            rateRepository.save(rate);
	        });
	        return "Prices updated successfully";
	    }

}

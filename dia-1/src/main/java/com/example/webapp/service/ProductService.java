package com.example.webapp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.webapp.models.Product;
import com.example.webapp.models.Rate;
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
    private AmazonS3 s3Client;
	
	private final String bucketName = "elasticbeanstalk-ap-south-1-012676044441"; // Replace with your bucket name
    private final String region = "ap-south-1"; // Replace with your AWS region (e.g., "us-east-1")

	
	public Product findById(int a) {
		
		return new Product();
	}
	
	public void addProduct(Product product) {
		System.out.println("product values are "+product.getProductId()+" "+product.getItem());
		System.out.println("design no "+product.getDesignNo());
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
		        productRepository.deleteByDesignNo(productId);
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

	public Optional<Product> findProductById(String productId) {
	        return productRepository.findByDesignNo(productId);
    }
	
	public Optional<Product> findProductFromId(Long productId) {
        return productRepository.findById(productId);
}
	
	public Product updateProduct(String productId, Product updatedProduct, MultipartFile imageFile) {
		System.out.println("inside service");
		System.out.println("design no is "+productId);
		System.out.println("new design no is "+updatedProduct.getDesignNo());
	    Optional<Product> existingProductOpt = productRepository.findByDesignNo(productId);
	    Optional<Product> alProduct = productRepository.findByDesignNo(updatedProduct.getDesignNo());
	    if (alProduct.isPresent()) {
	    	 Product alreadyProduct = alProduct.get();
	    //	 Product existProduct = existingProductOpt.get();
	    	if(!alreadyProduct.getDesignNo().equals(productId)) {
	    		System.out.println("came inside");
	        throw new IllegalArgumentException("Design number already exists for a different product");
	    }
	    }
	    if (existingProductOpt.isPresent()) {
	    	//System.out.println("product found");
	        Product existingProduct = existingProductOpt.get();
	        // Save the image if a new file isexistingProduct uploaded
	        if (imageFile != null && !imageFile.isEmpty()) {
	            String imageUrl = saveImageFile(imageFile);
	            updatedProduct.setImageUrl(imageUrl);
	        } else {
	            // If no new image is uploaded, keep the existing image URL
	            existingProduct.setImageUrl(existingProduct.getImageUrl());
	        }
	        
	        if(updatedProduct.getCategoryId()==1) {
	        	existingProduct.setItem(updatedProduct.getItem());
	        	existingProduct.setPrice(updatedProduct.getPrice());
	        	existingProduct.setRemarks(updatedProduct.getRemarks());
	        	existingProduct.setImageUrl(updatedProduct.getImageUrl());
	        	existingProduct.setCategoryId(updatedProduct.getCategoryId());
	        	existingProduct.setNet(updatedProduct.getNet());
	        	existingProduct.setPcs(updatedProduct.getPcs());
	        	existingProduct.setDiaWeight(updatedProduct.getDiaWeight());
	        	}
	        	if(updatedProduct.getCategoryId()==2) {
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
	        	}
	        	if(updatedProduct.getCategoryId()==3) {
	             	existingProduct.setItem(updatedProduct.getItem());
		        	existingProduct.setPrice(updatedProduct.getPrice());
		        	existingProduct.setRemarks(updatedProduct.getRemarks());
		        	existingProduct.setImageUrl(updatedProduct.getImageUrl());
		        	existingProduct.setCategoryId(updatedProduct.getCategoryId());
		        	existingProduct.setNet(updatedProduct.getNet());
	        		existingProduct.setDesignNo(updatedProduct.getDesignNo());
	        	}
	        	if(updatedProduct.getCategoryId()==4) {
	             	existingProduct.setItem(updatedProduct.getItem());
		        	existingProduct.setPrice(updatedProduct.getPrice());
		        	existingProduct.setRemarks(updatedProduct.getRemarks());
		        	existingProduct.setImageUrl(updatedProduct.getImageUrl());
		        	existingProduct.setCategoryId(updatedProduct.getCategoryId());
		        	existingProduct.setNet(updatedProduct.getNet());
		        	existingProduct.setDesignNo(updatedProduct.getDesignNo());
		        	existingProduct.setPcs(updatedProduct.getPcs());
	        		existingProduct.setDiamondsCt(updatedProduct.getDiamondsCt());
	        	}
	        	if(updatedProduct.getCategoryId()==5) {
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
	        	}
	        	if(updatedProduct.getCategoryId()==6) {
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
	        	}
	        return productRepository.save(existingProduct);
	    }
	    return null; // Or throw an exception as appropriate
	}
	
	public static ModelMapper getConfiguredModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true); // Skip null values
        return modelMapper;
    }
	
	/*
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
	    }*/
	
	 public String saveImageFile(MultipartFile file) {
	        File fileObj = convertMultiPartFileToFile(file);
	        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
	        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj).withCannedAcl(CannedAccessControlList.PublicRead));
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


	  public Page<Product> findByCategory(Integer category, Pageable pageable) {
	        return productRepository.findByCategoryId(category, pageable);
	    }
	  
	  public List<Rate> getAllRates() {
	        return rateRepository.findAll();
	    }

	  public String updatePrices(Map<String, BigDecimal> prices) {
		    prices.forEach((commodity, price) -> {
		        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
		            Rate rate = rateRepository.findByCommodity(commodity)
		                                       .orElse(new Rate());  // Create new Rate if commodity doesn't exist
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
	  
}

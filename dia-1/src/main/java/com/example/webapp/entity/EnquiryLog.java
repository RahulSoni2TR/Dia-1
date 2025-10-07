package com.example.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "enquiry_log")
public class EnquiryLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "design_no")
  private String designNo;

  @Column(name = "customer_name")
  private String customerName;

  @Column(name = "image_url")
  private String imageUrl;

  public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public String getDesignNo() {
	return designNo;
}

public void setDesignNo(String designNo) {
	this.designNo = designNo;
}

public String getCustomerName() {
	return customerName;
}

public void setCustomerName(String customerName) {
	this.customerName = customerName;
}

public String getImageUrl() {
	return imageUrl;
}

public void setImageUrl(String imageUrl) {
	this.imageUrl = imageUrl;
}

public int getProductId() {
	return productId;
}

public void setProductId(int productId) {
	this.productId = productId;
}

public LocalDateTime getCreatedAt() {
	return createdAt;
}

public void setCreatedAt(LocalDateTime createdAt) {
	this.createdAt = createdAt;
}

public String getProductSnapshot() {
	return productSnapshot;
}

public void setProductSnapshot(String productSnapshot) {
	this.productSnapshot = productSnapshot;
}

public Double getPrice() {
	return price;
}

public void setPrice(Double price) {
	this.price = price;
}

public Double getPriceWithFields() {
	return priceWithFields;
}

public void setPriceWithFields(Double priceWithFields) {
	this.priceWithFields = priceWithFields;
}

public String getEstimateSnapshot() {
	return estimateSnapshot;
}

public void setEstimateSnapshot(String estimateSnapshot) {
	this.estimateSnapshot = estimateSnapshot;
}

@Column(name = "product_id")
  private int productId;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  // Frozen server-side product snapshot (JSON)
  @Column(name = "product_snapshot", columnDefinition = "json")
  private String productSnapshot;

  // New: explicit prices (DOUBLE)
  @Column(name = "price")
  private Double price;

  @Column(name = "price_with_fields")
  private Double priceWithFields;

  // New: full client-side estimate snapshot (JSON)
  @Column(name = "estimate_snapshot", columnDefinition = "json")
  private String estimateSnapshot;

  // getters/setters (Lombok @Data already provides; keep custom ones if needed)
}

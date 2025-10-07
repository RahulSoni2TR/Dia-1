package com.example.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sales_log")
public class SalesLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private int productId;

  @Column(name = "design_no")
  private String designNo;

  // explicit prices
  @Column(name = "price")
  private Double price;

  public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public int getProductId() {
	return productId;
}

public void setProductId(int productId) {
	this.productId = productId;
}

public String getDesignNo() {
	return designNo;
}

public void setDesignNo(String designNo) {
	this.designNo = designNo;
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

public String getOrderId() {
	return orderId;
}

public void setOrderId(String orderId) {
	this.orderId = orderId;
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

@Column(name = "price_with_fields")
  private Double priceWithFields;

  // complete estimate snapshot (JSON)
  @Column(name = "estimate_snapshot", columnDefinition = "json")
  private String estimateSnapshot;

  @Column(name = "order_id")
  private String orderId;

  @Column(name = "customer_name")
  private String customerName;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "product_snapshot", columnDefinition = "json")
  private String productSnapshot;
}

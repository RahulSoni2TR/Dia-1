package com.example.webapp.models;

import lombok.Data;

@Data
public class LogRequest {
 private Long productId;
 private String designNo;
 private String orderId;      // optional for enquiry
 private String customerName;
 private String imageUrl;
 private Double price;
 private Double priceWithFields;

 // new nested snapshot
 private EstimateSnapshotDTO estimateSnapshot;
 
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
public EstimateSnapshotDTO getEstimateSnapshot() {
	return estimateSnapshot;
}
public void setEstimateSnapshot(EstimateSnapshotDTO estimateSnapshot) {
	this.estimateSnapshot = estimateSnapshot;
}
public Long getProductId() {
	return productId;
}
public void setProductId(Long productId) {
	this.productId = productId;
}
public String getDesignNo() {
	return designNo;
}
public void setDesignNo(String designNo) {
	this.designNo = designNo;
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

@Override
public String toString() {
  return "LogRequest{" +
      "productId=" + productId +
      ", designNo='" + designNo + '\'' +
      ", orderId='" + orderId + '\'' +
      ", customerName='" + customerName + '\'' +
      ", imageUrl='" + imageUrl + '\'' +
      ", price=" + price +
      ", priceWithFields=" + priceWithFields +
      ", estimateSnapshot=" + (estimateSnapshot != null ? estimateSnapshot : "null") +
      '}';
}

}

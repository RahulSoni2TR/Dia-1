package com.example.webapp.models;

public class EstimateSnapshotDTO {
	  private Long productId;
	  private String designNo;
	  private String orderId;
	  private String item;
	  private Integer categoryId;
	  private Integer subCategoryId;
	  private Double price;
	  private Double priceWithFields;
	  private Boolean includeAddons;
	  private TotalsDTO totals;
	  private EstimatePartsDTO estimateParts;
	  private java.util.List<EstimateLineDTO> lines;
	  private String imageUrl;
	  private String customerName;
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
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public Integer getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	public Integer getSubCategoryId() {
		return subCategoryId;
	}
	public void setSubCategoryId(Integer subCategoryId) {
		this.subCategoryId = subCategoryId;
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
	public Boolean getIncludeAddons() {
		return includeAddons;
	}
	public void setIncludeAddons(Boolean includeAddons) {
		this.includeAddons = includeAddons;
	}
	public TotalsDTO getTotals() {
		return totals;
	}
	public void setTotals(TotalsDTO totals) {
		this.totals = totals;
	}
	public EstimatePartsDTO getEstimateParts() {
		return estimateParts;
	}
	public void setEstimateParts(EstimatePartsDTO estimateParts) {
		this.estimateParts = estimateParts;
	}
	public java.util.List<EstimateLineDTO> getLines() {
		return lines;
	}
	public void setLines(java.util.List<EstimateLineDTO> lines) {
		this.lines = lines;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public java.util.List<java.util.Map<String, Object>> getRatesContext() {
		return ratesContext;
	}
	public void setRatesContext(java.util.List<java.util.Map<String, Object>> ratesContext) {
		this.ratesContext = ratesContext;
	}
	public String getClientTimestamp() {
		return clientTimestamp;
	}
	public void setClientTimestamp(String clientTimestamp) {
		this.clientTimestamp = clientTimestamp;
	}
	private java.util.List<java.util.Map<String,Object>> ratesContext;
	  private String clientTimestamp;

	  
	  @Override
	  public String toString() {
	    return "EstimateSnapshotDTO{" +
	        "productId=" + productId +
	        ", designNo='" + designNo + '\'' +
	        ", orderId='" + orderId + '\'' +
	        ", item='" + item + '\'' +
	        ", categoryId=" + categoryId +
	        ", subCategoryId=" + subCategoryId +
	        ", price=" + price +
	        ", priceWithFields=" + priceWithFields +
	        ", includeAddons=" + includeAddons +
	        ", totals=" + totals +
	        ", estimateParts=" + estimateParts +
	        ", lines=" + lines +
	        ", imageUrl='" + imageUrl + '\'' +
	        ", customerName='" + customerName + '\'' +
	        ", ratesContext=" + ratesContext +
	        ", clientTimestamp='" + clientTimestamp + '\'' +
	        '}';
	  }

	  // getters/setters ...
	  // generate all, omitted here for brevity
	}



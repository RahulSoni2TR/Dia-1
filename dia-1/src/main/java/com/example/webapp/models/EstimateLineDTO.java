package com.example.webapp.models;

public class EstimateLineDTO {
	 private String description;
	  private Double qty;
	  private Double rate;
	  public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Double getQty() {
		return qty;
	}
	public void setQty(Double qty) {
		this.qty = qty;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	private Double amount;
	
	@Override
	public String toString() {
	  return "EstimateLineDTO{" +
	      "description='" + description + '\'' +
	      ", qty=" + qty +
	      ", rate=" + rate +
	      ", amount=" + amount +
	      '}';
	}

}

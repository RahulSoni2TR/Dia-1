package com.example.webapp.models;

public class TotalsDTO {
	 private Double noGst;
	  private Double gst;
	  private Double grandTotal;
	public Double getNoGst() {
		return noGst;
	}
	public void setNoGst(Double noGst) {
		this.noGst = noGst;
	}
	public Double getGst() {
		return gst;
	}
	public void setGst(Double gst) {
		this.gst = gst;
	}
	public Double getGrandTotal() {
		return grandTotal;
	}
	public void setGrandTotal(Double grandTotal) {
		this.grandTotal = grandTotal;
	}
}

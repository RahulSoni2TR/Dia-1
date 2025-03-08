package com.example.webapp.models;

import java.math.BigDecimal;

public class RateWrapper {
    public BigDecimal goldPrice;
    public BigDecimal diamondPrice;
    public BigDecimal vilandiPrice;
    public  BigDecimal gst;
	public BigDecimal labourPrice;
	public BigDecimal silver;
	
	 public void resetRates() {
	        this.goldPrice = null;
	        this.diamondPrice = null;
	        this.vilandiPrice = null;
	        this.gst = null;
	        this.labourPrice = null;
	        this.silver = null;
	    }
}

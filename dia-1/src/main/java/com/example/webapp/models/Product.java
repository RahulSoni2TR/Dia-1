package com.example.webapp.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use AUTO_INCREMENT
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @NotNull(message = "Product name cannot be null")
    @Column(name = "item", nullable = false, length = 100)
    private String item;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @NotNull(message = "Stock quantity cannot be null")
    @Positive(message = "Stock quantity must be positive")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "net")
    private BigDecimal net;

    @Column(name = "pcs")
    private Integer pcs;

    @Column(name = "dia_weight")
    private BigDecimal diaWeight;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "gross")
    private BigDecimal gross;

    @Column(name = "vilandi_ct")
    private BigDecimal vilandiCt;

    @Column(name = "diamonds_ct")
    private BigDecimal diamondsCt;

    @Column(name = "beads_ct")
    private BigDecimal beadsCt;

    @Column(name = "pearls_gm")
    private BigDecimal pearlsGm;

    @Column(name = "other_stones_ct")
    private BigDecimal otherStonesCt;

    @Column(name = "others")
    private String others;

    @Column(name = "design_no")
    private String designNo;

    @Column(name = "stones")
    private BigDecimal stones;

    @Column(name = "ss_pearl_ct")
    private BigDecimal ssPearlCt;

    @Column(name = "real_stone")
    private BigDecimal realStone;

    // New fields for rates and additional fields
    @Column(name = "st_rate")
    private BigDecimal stRate;

    @Column(name = "bd_rate")
    private BigDecimal bdRate;

    @Column(name = "prl_rate")
    private BigDecimal prlRate;

    @Column(name = "ss_rate")
    private BigDecimal ssRate;

    @Column(name = "fitting")
    private BigDecimal fitting;

    @Column(name = "mozonite")
    private BigDecimal mozonite;
    
    @Column(name = "vilandi_rate")
    private BigDecimal vRate;

	public BigDecimal getvRate() {
		return vRate;
	}

	public void setvRate(BigDecimal vRate) {
		this.vRate = vRate;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public BigDecimal getNet() {
		return net;
	}

	public void setNet(BigDecimal net) {
		this.net = net;
	}

	public Integer getPcs() {
		return pcs;
	}

	public void setPcs(Integer pcs) {
		this.pcs = pcs;
	}

	public BigDecimal getDiaWeight() {
		return diaWeight;
	}

	public void setDiaWeight(BigDecimal diaWeight) {
		this.diaWeight = diaWeight;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public BigDecimal getGross() {
		return gross;
	}

	public void setGross(BigDecimal gross) {
		this.gross = gross;
	}

	public BigDecimal getVilandiCt() {
		return vilandiCt;
	}

	public void setVilandiCt(BigDecimal vilandiCt) {
		this.vilandiCt = vilandiCt;
	}

	public BigDecimal getDiamondsCt() {
		return diamondsCt;
	}

	public void setDiamondsCt(BigDecimal diamondsCt) {
		this.diamondsCt = diamondsCt;
	}

	public BigDecimal getBeadsCt() {
		return beadsCt;
	}

	public void setBeadsCt(BigDecimal beadsCt) {
		this.beadsCt = beadsCt;
	}

	public BigDecimal getPearlsGm() {
		return pearlsGm;
	}

	public void setPearlsGm(BigDecimal pearlsGm) {
		this.pearlsGm = pearlsGm;
	}

	public BigDecimal getOtherStonesCt() {
		return otherStonesCt;
	}

	public void setOtherStonesCt(BigDecimal otherStonesCt) {
		this.otherStonesCt = otherStonesCt;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public String getDesignNo() {
		return designNo;
	}

	public void setDesignNo(String designNo) {
		this.designNo = designNo;
	}

	public BigDecimal getStones() {
		return stones;
	}

	public void setStones(BigDecimal stones) {
		this.stones = stones;
	}

	public BigDecimal getSsPearlCt() {
		return ssPearlCt;
	}

	public void setSsPearlCt(BigDecimal ssPearlCt) {
		this.ssPearlCt = ssPearlCt;
	}

	public BigDecimal getRealStone() {
		return realStone;
	}

	public void setRealStone(BigDecimal realStone) {
		this.realStone = realStone;
	}

	public BigDecimal getStRate() {
		return stRate;
	}

	public void setStRate(BigDecimal stRate) {
		this.stRate = stRate;
	}

	public BigDecimal getBdRate() {
		return bdRate;
	}

	public void setBdRate(BigDecimal bdRate) {
		this.bdRate = bdRate;
	}

	public BigDecimal getPrlRate() {
		return prlRate;
	}

	public void setPrlRate(BigDecimal prlRate) {
		this.prlRate = prlRate;
	}

	public BigDecimal getSsRate() {
		return ssRate;
	}

	public void setSsRate(BigDecimal ssRate) {
		this.ssRate = ssRate;
	}

	public BigDecimal getFitting() {
		return fitting;
	}

	public void setFitting(BigDecimal fitting) {
		this.fitting = fitting;
	}

	public BigDecimal getMozonite() {
		return mozonite;
	}

	public void setMozonite(BigDecimal mozonite) {
		this.mozonite = mozonite;
	}

  
}

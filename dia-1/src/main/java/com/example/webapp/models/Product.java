package com.example.webapp.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    @Column(name = "item", nullable = true, length = 100)
    private String item;

    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = true)
    private BigDecimal price;

    @Positive(message = "Stock quantity must be positive")
    @Column(name = "stock_quantity", nullable = true)
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
    
    @Column(name = "diamond_rate")
    private BigDecimal diaRt;

    @Column(name = "remarks")
    private String remarks;

    public BigDecimal getDiaRt() {
		return diaRt;
	}


	public void setDiaRt(BigDecimal diaRt) {
		this.diaRt = diaRt;
	}


	public BigDecimal getOtherStonesRt() {
		return otherStonesRt;
	}


	public void setOtherStonesRt(BigDecimal otherStonesRt) {
		this.otherStonesRt = otherStonesRt;
	}

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

    @Column(name = "other_stones_rate")
    private BigDecimal otherStonesRt;
    
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

    @Column(name = "m_rate")
    private BigDecimal mRate;
    
    @Column(name = "labour_gm")
    private BigDecimal labour;
    
    @Column(name = "labour_all")
    private BigDecimal labourAll;
    
    
    @Column(name = "karat")
    private BigDecimal karat;
    

    public void resetProduct() {
        this.item = null;
        this.price = null;
        this.stockQuantity = null;
        this.categoryId = null;
        this.imageUrl = null;
        this.net = null;
        this.pcs = null;
        this.diaWeight = null;
        this.remarks = null;
        this.gross = null;
        this.vilandiCt = null;
        this.diamondsCt = null;
        this.beadsCt = null;
        this.pearlsGm = null;
        this.otherStonesCt = null;
        this.others = null;
        this.designNo = null;
        this.stones = null;
        this.ssPearlCt = null;
        this.realStone = null;

        // Reset rates and additional fields
        this.stRate = null;
        this.bdRate = null;
        this.prlRate = null;
        this.ssRate = null;
        this.fitting = null;
        this.mozonite = null;
        this.mRate = null;
        this.labour = null;
        this.labourAll = null;
        this.karat = null;
    }

    
	public void setLabour(BigDecimal labour) {
		this.labour = labour;
	}

    public BigDecimal getLabour() {
		return labour;
	}
	
	public BigDecimal getLabourAll() {
		return labourAll;
	}

	public void setLabourAll(BigDecimal labourAll) {
		this.labourAll = labourAll;
	}

	public BigDecimal getKarat() {
		return karat;
	}

	public void setKarat(BigDecimal karat) {
		this.karat = karat;
	}

	@Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime;
    
	@Column(name = "update_date_time")
    private LocalDateTime updateDateTime;

    public LocalDateTime getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(LocalDateTime createDateTime) {
		this.createDateTime = createDateTime;
	}

	public LocalDateTime getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(LocalDateTime updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

    
    public BigDecimal getmRate() {
		return mRate;
	}

	public void setmRate(BigDecimal mRate) {
		this.mRate = mRate;
	}

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

	@PrePersist
	protected void onCreate() {
	    ZoneId zoneId = ZoneId.of("Asia/Kolkata"); // IST Timezone
	    createDateTime = ZonedDateTime.now(zoneId).toLocalDateTime();
	    updateDateTime = ZonedDateTime.now(zoneId).toLocalDateTime();
	}

	@PreUpdate
	protected void onUpdate() {
	    ZoneId zoneId = ZoneId.of("Asia/Kolkata"); // IST Timezone
	    updateDateTime = ZonedDateTime.now(zoneId).toLocalDateTime();
	}
	
	 @Override
	    public String toString() {
	        return "Product{" +
	               "productId=" + productId +
	               ", item='" + item + '\'' +
	               ", price=" + price +
	               ", stockQuantity=" + stockQuantity +
	               ", categoryId=" + categoryId +
	               ", imageUrl='" + imageUrl + '\'' +
	               ", net=" + net +
	               ", pcs=" + pcs +
	               ", diaWeight=" + diaWeight +
	               ", remarks='" + remarks + '\'' +
	               ", gross=" + gross +
	               ", vilandiCt=" + vilandiCt +
	               ", diamondsCt=" + diamondsCt +
	               ", beadsCt=" + beadsCt +
	               ", pearlsGm=" + pearlsGm +
	               ", otherStonesCt=" + otherStonesCt +
	               ", others='" + others + '\'' +
	               ", designNo='" + designNo + '\'' +
	               ", stones=" + stones +
	               ", ssPearlCt=" + ssPearlCt +
	               ", realStone=" + realStone +
	               ", stRate=" + stRate +
	               ", bdRate=" + bdRate +
	               ", prlRate=" + prlRate +
	               ", ssRate=" + ssRate +
	               ", fitting=" + fitting +
	               ", mozonite=" + mozonite +
	               ", mRate=" + mRate +
	               ", vRate=" + vRate +
	               '}';
	    }
  
}

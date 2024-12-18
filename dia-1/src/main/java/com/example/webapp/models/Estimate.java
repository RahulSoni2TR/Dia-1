package com.example.webapp.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Estimate {
    private String estimateId; // Unique identifier for the estimate
    private BigDecimal gold;
    private BigDecimal labour;
    private BigDecimal stones;
    private BigDecimal beads;
    private BigDecimal pearls;
    private BigDecimal ssPearls; // "ss pearls"
    private BigDecimal vilandi;
    private BigDecimal mozo;
    private BigDecimal realStones; // "real stones"
    private BigDecimal fitting;
    private BigDecimal gst;
    private BigDecimal nogsttotal;
    private BigDecimal total;

    // No-argument constructor
    public Estimate() {
        this.gold = BigDecimal.ZERO;
        this.labour = BigDecimal.ZERO;
        this.stones = BigDecimal.ZERO;
        this.beads = BigDecimal.ZERO;
        this.pearls = BigDecimal.ZERO;
        this.ssPearls = BigDecimal.ZERO;
        this.mozo = BigDecimal.ZERO;
        this.realStones = BigDecimal.ZERO;
        this.fitting = BigDecimal.ZERO;
        this.gst = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.vilandi = BigDecimal.ZERO;
        this.nogsttotal = BigDecimal.ZERO;
    }

    // Parameterized constructor
    public Estimate(String estimateId, BigDecimal gold, BigDecimal labour, BigDecimal stones,
                   BigDecimal beads, BigDecimal pearls, BigDecimal ssPearls,BigDecimal vilandi, BigDecimal mozo,
                   BigDecimal realStones, BigDecimal fitting, BigDecimal gst,BigDecimal nogsttotal) {
        this.estimateId = estimateId;
        this.gold = gold;
        this.labour = labour;
        this.stones = stones;
        this.beads = beads;
        this.pearls = pearls;
        this.ssPearls = ssPearls;
        this.vilandi = vilandi;
        this.mozo = mozo;
        this.realStones = realStones;
        this.fitting = fitting;
        this.gst = gst;
        this.nogsttotal = nogsttotal;
    }

    // Getters and Setters

    public String getEstimateId() {
        return estimateId;
    }

    public void setEstimateId(String estimateId) {
        this.estimateId = estimateId;
    }

    public BigDecimal getGold() {
        return gold;
    }

    public void setGold(BigDecimal gold) {
        this.gold = gold.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getLabour() {
        return labour;
    }

    public void setLabour(BigDecimal labour) {
        this.labour = labour.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getStones() {
        return stones;
    }

    public void setStones(BigDecimal stones) {
        this.stones = stones.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getBeads() {
        return beads;
    }

    public void setBeads(BigDecimal beads) {
        this.beads = beads.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getPearls() {
        return pearls;
    }

    public void setPearls(BigDecimal pearls) {
        this.pearls = pearls.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getSsPearls() {
        return ssPearls;
    }

    public BigDecimal getVilandi() {
		return vilandi;
	}

	public void setVilandi(BigDecimal vilandi) {
		this.vilandi = vilandi.setScale(0, RoundingMode.HALF_UP);
	}

	public void setSsPearls(BigDecimal ssPearls) {
        this.ssPearls = ssPearls.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getMozo() {
        return mozo;
    }

    public void setMozo(BigDecimal mozo) {
        this.mozo = mozo.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getRealStones() {
        return realStones;
    }

    public void setRealStones(BigDecimal realStones) {
        this.realStones = realStones.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getFitting() {
        return fitting;
    }

    public void setFitting(BigDecimal fitting) {
        this.fitting = fitting.setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst.setScale(0, RoundingMode.HALF_UP);
    }

    public void setTotal(BigDecimal total) {
		this.total = total.setScale(0, RoundingMode.HALF_UP);
	}

	public BigDecimal getTotal() {
        return total;
    }

    

    @Override
    public String toString() {
        return "Estimate{" +
                "estimateId='" + estimateId + '\'' +
                ", gold=" + gold +
                ", labour=" + labour +
                ", stones=" + stones +
                ", beads=" + beads +
                ", pearls=" + pearls +
                ", ssPearls=" + ssPearls +
                ", mozo=" + mozo +
                ", realStones=" + realStones +
                ", fitting=" + fitting +
                ", gst=" + gst +
                ", total=" + total +
                '}';
    }

	public BigDecimal getNogsttotal() {
		return nogsttotal;
	}

	public void setNogsttotal(BigDecimal nogsttotal) {
		this.nogsttotal = nogsttotal.setScale(0, RoundingMode.HALF_UP);
	}
}

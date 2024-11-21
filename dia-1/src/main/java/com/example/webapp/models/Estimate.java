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
    private BigDecimal mozo;
    private BigDecimal realStones; // "real stones"
    private BigDecimal fitting;
    private BigDecimal gst;
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
    }

    // Parameterized constructor
    public Estimate(String estimateId, BigDecimal gold, BigDecimal labour, BigDecimal stones,
                   BigDecimal beads, BigDecimal pearls, BigDecimal ssPearls, BigDecimal mozo,
                   BigDecimal realStones, BigDecimal fitting, BigDecimal gst) {
        this.estimateId = estimateId;
        this.gold = gold;
        this.labour = labour;
        this.stones = stones;
        this.beads = beads;
        this.pearls = pearls;
        this.ssPearls = ssPearls;
        this.mozo = mozo;
        this.realStones = realStones;
        this.fitting = fitting;
        this.gst = gst;
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
        this.gold = gold;
    }

    public BigDecimal getLabour() {
        return labour;
    }

    public void setLabour(BigDecimal labour) {
        this.labour = labour;
    }

    public BigDecimal getStones() {
        return stones;
    }

    public void setStones(BigDecimal stones) {
        this.stones = stones;
    }

    public BigDecimal getBeads() {
        return beads;
    }

    public void setBeads(BigDecimal beads) {
        this.beads = beads;
    }

    public BigDecimal getPearls() {
        return pearls;
    }

    public void setPearls(BigDecimal pearls) {
        this.pearls = pearls;
    }

    public BigDecimal getSsPearls() {
        return ssPearls;
    }

    public void setSsPearls(BigDecimal ssPearls) {
        this.ssPearls = ssPearls;
    }

    public BigDecimal getMozo() {
        return mozo;
    }

    public void setMozo(BigDecimal mozo) {
        this.mozo = mozo;
    }

    public BigDecimal getRealStones() {
        return realStones;
    }

    public void setRealStones(BigDecimal realStones) {
        this.realStones = realStones;
    }

    public BigDecimal getFitting() {
        return fitting;
    }

    public void setFitting(BigDecimal fitting) {
        this.fitting = fitting;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public void setTotal(BigDecimal total) {
		this.total = total;
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
}

package com.example.webapp.models;

public class EstimatePartsDTO {
	 private Double gold;
	  private Double labour;
	  private Double stones;
	  private Double beads;
	  private Double pearls;
	  private Double ssPearls;
	  private Double diamonds;
	  private Double otherStones;
	  private Double vilandi;
	  private Double mozo;
	  private Double fitting;
	  private Double realStone;

	public Double getRealStone() {
		return realStone;
	}
	public void setRealStone(Double realStone) {
		this.realStone = realStone;
	}
	public Double getGold() {
		return gold;
	}
	public void setGold(Double gold) {
		this.gold = gold;
	}
	public Double getLabour() {
		return labour;
	}
	public void setLabour(Double labour) {
		this.labour = labour;
	}
	public Double getStones() {
		return stones;
	}
	public void setStones(Double stones) {
		this.stones = stones;
	}
	public Double getBeads() {
		return beads;
	}
	public void setBeads(Double beads) {
		this.beads = beads;
	}
	public Double getPearls() {
		return pearls;
	}
	public void setPearls(Double pearls) {
		this.pearls = pearls;
	}
	public Double getSsPearls() {
		return ssPearls;
	}
	public void setSsPearls(Double ssPearls) {
		this.ssPearls = ssPearls;
	}
	public Double getDiamonds() {
		return diamonds;
	}
	public void setDiamonds(Double diamonds) {
		this.diamonds = diamonds;
	}
	public Double getOtherStones() {
		return otherStones;
	}
	public void setOtherStones(Double otherStones) {
		this.otherStones = otherStones;
	}
	public Double getVilandi() {
		return vilandi;
	}
	public void setVilandi(Double vilandi) {
		this.vilandi = vilandi;
	}
	public Double getMozo() {
		return mozo;
	}
	public void setMozo(Double mozo) {
		this.mozo = mozo;
	}
	public Double getFitting() {
		return fitting;
	}
	public void setFitting(Double fitting) {
		this.fitting = fitting;
	}
	
	@Override
	public String toString() {
	  return "EstimatePartsDTO{" +
	      "gold=" + gold +
	      ", labour=" + labour +
	      ", stones=" + stones +
	      ", beads=" + beads +
	      ", pearls=" + pearls +
	      ", ssPearls=" + ssPearls +
	      ", diamonds=" + diamonds +
	      ", otherStones=" + otherStones +
	      ", vilandi=" + vilandi +
	      ", mozo=" + mozo +
	      ", fitting=" + fitting +
	      '}';
	}

}

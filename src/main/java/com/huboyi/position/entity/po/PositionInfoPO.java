package com.huboyi.position.entity.po;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

/**
 * 持仓信息PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class PositionInfoPO implements Serializable {

	private static final long serialVersionUID = 3146330724412870775L;

	@Id
	/** id */
	private long id;
	
	// --- 
	/** 证券代码。*/
	private String stockCode;
	/** 证券名称。*/
	private String stockName;
	/** 证券数量。*/
	private Long stockNumber;
	/** 可卖数量。*/
	private Long canSellNumber;
	
	// --- 
	/** 成本价。*/
	private BigDecimal costPrice;
	/** 成本金额。*/
	private BigDecimal costMoney;
	
	// --- 
	/** 当前价。*/
	private BigDecimal newPrice;
	/** 最新市值。*/
	private BigDecimal newMarketValue;
	/** 浮动盈亏。*/
	private BigDecimal floatProfitAndLoss;
	/** 盈亏比例。*/
	private BigDecimal profitAndLossRatio;
	
	// --- 
	/** 今买数量。*/
	private Long todayBuyNumber = 0L;
	/** 今卖数量。*/
	private Long todaySellNumber = 0L;
	
	// --- 
	/** 股东代码。*/
	private String stockholder = "672288";
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		.append("    ").append("id").append(":").append("'").append(id).append("'").append(", \n")
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		.append("    ").append("stockNumber").append(":").append("'").append(stockNumber).append("'").append(", \n")
		.append("    ").append("canSellNumber").append(":").append("'").append(canSellNumber).append("'").append(", \n")
		.append("    ").append("costPrice").append(":").append("'").append(costPrice).append("'").append(", \n")
		.append("    ").append("costMoney").append(":").append("'").append(costMoney).append("'").append(", \n")
		.append("    ").append("newPrice").append(":").append("'").append(newPrice).append("'").append(", \n")
		.append("    ").append("newMarketValue").append(":").append("'").append(newMarketValue).append("'").append(", \n")
		.append("    ").append("floatProfitAndLoss").append(":").append("'").append(floatProfitAndLoss).append("'").append(", \n")
		.append("    ").append("profitAndLossRatio").append(":").append("'").append(profitAndLossRatio).append("'").append(", \n")
		.append("    ").append("todayBuyNumber").append(":").append("'").append(todayBuyNumber).append("'").append(", \n")
		.append("    ").append("todaySellNumber").append(":").append("'").append(todaySellNumber).append("'").append(", \n")
		.append("    ").append("stockholder").append(":").append("'").append(stockholder).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}
	
	// --- get method and set method ---
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}

	public Long getStockNumber() {
		return stockNumber;
	}

	public void setStockNumber(Long stockNumber) {
		this.stockNumber = stockNumber;
	}

	public Long getCanSellNumber() {
		return canSellNumber;
	}

	public void setCanSellNumber(Long canSellNumber) {
		this.canSellNumber = canSellNumber;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public BigDecimal getCostMoney() {
		return costMoney;
	}

	public void setCostMoney(BigDecimal costMoney) {
		this.costMoney = costMoney;
	}

	public BigDecimal getNewPrice() {
		return newPrice;
	}

	public void setNewPrice(BigDecimal newPrice) {
		this.newPrice = newPrice;
	}

	public BigDecimal getNewMarketValue() {
		return newMarketValue;
	}

	public void setNewMarketValue(BigDecimal newMarketValue) {
		this.newMarketValue = newMarketValue;
	}

	public BigDecimal getFloatProfitAndLoss() {
		return floatProfitAndLoss;
	}

	public void setFloatProfitAndLoss(BigDecimal floatProfitAndLoss) {
		this.floatProfitAndLoss = floatProfitAndLoss;
	}

	public BigDecimal getProfitAndLossRatio() {
		return profitAndLossRatio;
	}

	public void setProfitAndLossRatio(BigDecimal profitAndLossRatio) {
		this.profitAndLossRatio = profitAndLossRatio;
	}

	public Long getTodayBuyNumber() {
		return todayBuyNumber;
	}

	public void setTodayBuyNumber(Long todayBuyNumber) {
		this.todayBuyNumber = todayBuyNumber;
	}

	public Long getTodaySellNumber() {
		return todaySellNumber;
	}

	public void setTodaySellNumber(Long todaySellNumber) {
		this.todaySellNumber = todaySellNumber;
	}

	public String getStockholder() {
		return stockholder;
	}

	public void setStockholder(String stockholder) {
		this.stockholder = stockholder;
	}
}
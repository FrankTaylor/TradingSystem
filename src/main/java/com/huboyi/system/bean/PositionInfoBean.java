package com.huboyi.system.bean;

import java.math.BigDecimal;

/**
 * 用于记录每一笔持仓信息的Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/12
 * @version 1.0
 */
public abstract class PositionInfoBean {
	
	// --- 
	/** 系统名称。*/
	private String systemName;
	/** 证券代码。*/
	private String stockCode;
	/** 证券名称。*/
	private String stockName;

	// --- 
	/** 建仓合同编号。*/
	private String openContractCode;
	/** 系统建仓点（买入信号类型）。*/
	private String systemOpenPoint;
	/** 建仓信号发出时间。*/
	private Long openSignalTime;
	/** 建仓日期（格式：%Y%m%d）。*/
	private Integer openDate;
	/** 建仓时间（详细时间）。*/
	private Long openTime;
	/** 建仓价格。*/
	private BigDecimal openPrice;
	/** 建仓数量。*/
	private Long openNumber;
	/** 建仓成本。*/
	private BigDecimal openCost;
	
	// --- 
	/** 可平仓数量。*/
	private Long canCloseNumber;
	/** 止损价格。*/
	private BigDecimal stopPrice;
	
	// --- 
	/** 平仓合同编号。*/
	private String closeContractCode;
	/** 系统平仓点（卖出信号类型）。*/
	private String systemClosePoint;
	/** 平仓信号发出时间。*/
	private Long closeSignalTime;
	/** 平仓日期（格式：%Y%m%d）。*/
	private Integer closeDate;
	/** 平仓时间（详细时间）。*/
	private Long closeTime;
	/** 平仓价格。*/
	private BigDecimal closePrice;
	/** 平仓数量。*/
	private Long closeNumber;
	
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
	/** 股东代码。*/
	private String stockholder;
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		
		// --- 
		.append("    ").append("systemName").append(":").append("'").append(systemName).append("'").append(", \n")
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		
		// --- 
		.append("    ").append("openContractCode").append(":").append("'").append(openContractCode).append("'").append(", \n")
		.append("    ").append("systemOpenPoint").append(":").append("'").append(systemOpenPoint).append("'").append(", \n")
		.append("    ").append("openSignalTime").append(":").append("'").append(openSignalTime).append("'").append(", \n")
		.append("    ").append("openDate").append(":").append("'").append(openDate).append("'").append(", \n")
		.append("    ").append("openTime").append(":").append("'").append(openTime).append("'").append(", \n")
		.append("    ").append("openPrice").append(":").append("'").append(openPrice).append("'").append(", \n")
		.append("    ").append("openNumber").append(":").append("'").append(openNumber).append("'").append(", \n")
		.append("    ").append("openCost").append(":").append("'").append(openCost).append("'").append(", \n")
		
		// --- 
		.append("    ").append("canCloseNumber").append(":").append("'").append(canCloseNumber).append("'").append(", \n")
		.append("    ").append("stopPrice").append(":").append("'").append(stopPrice).append("'").append(", \n")
		
		// --- 
		.append("    ").append("closeContractCode").append(":").append("'").append(closeContractCode).append("'").append(", \n")
		.append("    ").append("systemClosePoint").append(":").append("'").append(systemClosePoint).append("'").append(", \n")
		.append("    ").append("closeSignalTime").append(":").append("'").append(closeSignalTime).append("'").append(", \n")
		.append("    ").append("closeDate").append(":").append("'").append(closeDate).append("'").append(", \n")
		.append("    ").append("closeTime").append(":").append("'").append(closeTime).append("'").append(", \n")
		.append("    ").append("closePrice").append(":").append("'").append(closePrice).append("'").append(", \n")
		.append("    ").append("closeNumber").append(":").append("'").append(closeNumber).append("'").append(", \n")
		
		// --- 
		.append("    ").append("newPrice").append(":").append("'").append(newPrice).append("'").append(", \n")
		.append("    ").append("newMarketValue").append(":").append("'").append(newMarketValue).append("'").append(", \n")
		.append("    ").append("floatProfitAndLoss").append(":").append("'").append(floatProfitAndLoss).append("'").append(", \n")
		.append("    ").append("profitAndLossRatio").append(":").append("'").append(profitAndLossRatio).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockholder").append(":").append("'").append(stockholder).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}

	// --- get method and set method ---
	
	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
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

	public String getOpenContractCode() {
		return openContractCode;
	}

	public void setOpenContractCode(String openContractCode) {
		this.openContractCode = openContractCode;
	}

	public String getSystemOpenPoint() {
		return systemOpenPoint;
	}

	public void setSystemOpenPoint(String systemOpenPoint) {
		this.systemOpenPoint = systemOpenPoint;
	}

	public Long getOpenSignalTime() {
		return openSignalTime;
	}

	public void setOpenSignalTime(Long openSignalTime) {
		this.openSignalTime = openSignalTime;
	}

	public Integer getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Integer openDate) {
		this.openDate = openDate;
	}

	public Long getOpenTime() {
		return openTime;
	}

	public void setOpenTime(Long openTime) {
		this.openTime = openTime;
	}

	public BigDecimal getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(BigDecimal openPrice) {
		this.openPrice = openPrice;
	}

	public Long getOpenNumber() {
		return openNumber;
	}

	public void setOpenNumber(Long openNumber) {
		this.openNumber = openNumber;
	}

	public BigDecimal getOpenCost() {
		return openCost;
	}

	public void setOpenCost(BigDecimal openCost) {
		this.openCost = openCost;
	}

	public Long getCanCloseNumber() {
		return canCloseNumber;
	}

	public void setCanCloseNumber(Long canCloseNumber) {
		this.canCloseNumber = canCloseNumber;
	}

	public BigDecimal getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(BigDecimal stopPrice) {
		this.stopPrice = stopPrice;
	}

	public String getCloseContractCode() {
		return closeContractCode;
	}

	public void setCloseContractCode(String closeContractCode) {
		this.closeContractCode = closeContractCode;
	}

	public String getSystemClosePoint() {
		return systemClosePoint;
	}

	public void setSystemClosePoint(String systemClosePoint) {
		this.systemClosePoint = systemClosePoint;
	}

	public Long getCloseSignalTime() {
		return closeSignalTime;
	}

	public void setCloseSignalTime(Long closeSignalTime) {
		this.closeSignalTime = closeSignalTime;
	}

	public Integer getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Integer closeDate) {
		this.closeDate = closeDate;
	}

	public Long getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Long closeTime) {
		this.closeTime = closeTime;
	}

	public BigDecimal getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(BigDecimal closePrice) {
		this.closePrice = closePrice;
	}

	public Long getCloseNumber() {
		return closeNumber;
	}

	public void setCloseNumber(Long closeNumber) {
		this.closeNumber = closeNumber;
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

	public String getStockholder() {
		return stockholder;
	}

	public void setStockholder(String stockholder) {
		this.stockholder = stockholder;
	}
}
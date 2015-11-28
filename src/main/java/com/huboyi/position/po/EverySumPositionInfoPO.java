package com.huboyi.position.po;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

/**
 * 每一笔持仓信息PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class EverySumPositionInfoPO {

	@Id
	/** id */
	private String id;
	
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
	/** 系统建仓点类型（买入信号类型）。*/
	private String systemOpenPoint;
	/** 系统建仓点名称（买入信号名称）。*/
	private String systemOpenName;
	/** 建仓信号发出时间（格式：yyyyMMddhhmmssSSS）。*/
	private Long openSignalDate;
	/** 建仓日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long openDate;
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
	private String closeContractCode = "no";
	/** 系统平仓点类型（卖出信号类型）。*/
	private String systemClosePoint;
	/** 系统平仓点名称（卖出信号名称）。*/
	private String systemCloseName;
	/** 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。*/
	private Long closeSignalDate;
	/** 平仓日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long closeDate;
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
	private String stockholder = "672288";
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		.append("    ").append("id").append(":").append("'").append(id).append("'").append(", \n")
		
		// --- 
		.append("    ").append("systemName").append(":").append("'").append(systemName).append("'").append(", \n")
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		
		// --- 
		.append("    ").append("openContractCode").append(":").append("'").append(openContractCode).append("'").append(", \n")
		.append("    ").append("systemOpenPoint").append(":").append("'").append(systemOpenPoint).append("'").append(", \n")
		.append("    ").append("systemOpenName").append(":").append("'").append(systemOpenName).append("'").append(", \n")
		.append("    ").append("openSignalDate").append(":").append("'").append(openSignalDate).append("'").append(", \n")
		.append("    ").append("openDate").append(":").append("'").append(openDate).append("'").append(", \n")
		.append("    ").append("openPrice").append(":").append("'").append(openPrice).append("'").append(", \n")
		.append("    ").append("openNumber").append(":").append("'").append(openNumber).append("'").append(", \n")
		.append("    ").append("openCost").append(":").append("'").append(openCost).append("'").append(", \n")
		
		// --- 
		.append("    ").append("canCloseNumber").append(":").append("'").append(canCloseNumber).append("'").append(", \n")
		.append("    ").append("stopPrice").append(":").append("'").append(stopPrice).append("'").append(", \n")
		
		// --- 
		.append("    ").append("closeContractCode").append(":").append("'").append(closeContractCode).append("'").append(", \n")
		.append("    ").append("systemClosePoint").append(":").append("'").append(systemClosePoint).append("'").append(", \n")
		.append("    ").append("systemCloseName").append(":").append("'").append(systemCloseName).append("'").append(", \n")
		.append("    ").append("closeSignalDate").append(":").append("'").append(closeSignalDate).append("'").append(", \n")
		.append("    ").append("closeDate").append(":").append("'").append(closeDate).append("'").append(", \n")
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
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getSystemOpenName() {
		return systemOpenName;
	}

	public void setSystemOpenName(String systemOpenName) {
		this.systemOpenName = systemOpenName;
	}

	public Long getOpenSignalDate() {
		return openSignalDate;
	}

	public void setOpenSignalDate(Long openSignalDate) {
		this.openSignalDate = openSignalDate;
	}

	public Long getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Long openDate) {
		this.openDate = openDate;
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

	public String getSystemCloseName() {
		return systemCloseName;
	}

	public void setSystemCloseName(String systemCloseName) {
		this.systemCloseName = systemCloseName;
	}

	public Long getCloseSignalDate() {
		return closeSignalDate;
	}

	public void setCloseSignalDate(Long closeSignalDate) {
		this.closeSignalDate = closeSignalDate;
	}

	public Long getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Long closeDate) {
		this.closeDate = closeDate;
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
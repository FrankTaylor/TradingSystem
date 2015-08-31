package com.huboyi.system.test.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.huboyi.system.module.fractal.signal.bean.FractalDealSignalBean;

/**
 * 测试结果信息。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/13
 * @version 1.0
 */
public class TestResultBean implements Serializable, Comparable<TestResultBean> {	

	private static final long serialVersionUID = -956430720957771546L;
	
	// --- 交易盈亏 ---
	/** 证券代码。*/
	private String stockCode;
	/** 总资产（剩余资金 + 股票市值）。*/
	private BigDecimal totalAsset;
	/** 浮动盈亏。*/
	private BigDecimal floatProfitAndLoss;
	/** 盈亏比例。*/
	private BigDecimal profitAndLossRatio;
	/** 胜率。*/
	private BigDecimal winRate;
	
	// --- 资产分布 ---
	/** 初始资金。*/
	private BigDecimal initMoney;
	/** 资金余额。*/
	private BigDecimal fundsBalance;
	/** 股票市值。*/
	private BigDecimal marketValue;
	/** 证券数量。*/
	private Long stockNumber;
	/** 成本价格。*/
	private BigDecimal costPrice;
	/** 当前价格。*/
	private BigDecimal newPrice;
	
	// --- 交易频率 ---
	/** 买卖详情。*/
	private List<String> dealDetailList;
	/** 交易次数。*/
	private Integer dealNumber;
	/** 买入次数。*/
	private Integer buyNumber;
	/** 卖出次数。*/
	private Integer sellNumber;
	/** 平均间隔。*/
	private BigDecimal avgBuyAndSellInterval;
	/** 最大间隔。*/
	private BigDecimal maxBuyAndSellInterval;
	/** 最小间隔。*/
	private BigDecimal minBuyAndSellInterval;
	
	// --- 交易周期 ---
	/** 盈亏详情。*/
	private List<BigDecimal> cyclePLDetailList;
	/** 交易周期。*/
	private Integer cycleNumber;
	/** 盈利周期。*/
	private Integer winNumber;
	/** 亏损周期。*/
	private Integer lossNumber;
	/** 平均间隔。*/
	private BigDecimal avgCycleInterval;
	/** 最大间隔。*/
	private BigDecimal maxCycleInterval;
	/** 最小间隔。*/
	private BigDecimal minCycleInterval;
	
	// --- 周期盈亏 ---
	/** 平均赢利。*/
	private BigDecimal avgProfit;
	/** 平均亏损。*/
	private BigDecimal avgLoss;
	/** 最大盈利。*/
	private BigDecimal maxProfit;
	/** 最大亏损。*/
	private BigDecimal maxLoss;
	/** 最小盈利。*/
	private BigDecimal minProfit;
	/** 最小亏损。*/
	private BigDecimal minLoss;
	
	// --- 最后一个交易信号 ---
	private FractalDealSignalBean lastDealSignal;

	@Override
	public int compareTo(TestResultBean o) {
		/*
		 * 大于0返回-1，小于0返回1。是因为需要对测试结果做倒叙。
		 */
		return (totalAsset.compareTo(o.getTotalAsset()) > 0) ? -1 : (totalAsset.compareTo(o.getTotalAsset())  < 0) ? 1 : 0;
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		// --- 交易盈亏 ---
		.append("\n ------ 交易盈亏 ------ \n")
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("totalAsset").append(":").append("'").append(totalAsset).append("'").append(", \n")
		.append("    ").append("floatProfitAndLoss").append(":").append("'").append(floatProfitAndLoss).append("'").append(", \n")
		.append("    ").append("profitAndLossRatio").append(":").append("'").append(profitAndLossRatio).append("'").append(", \n")
		.append("    ").append("winRate").append(":").append("'").append(winRate).append("'").append(", \n")
		
		// --- 资产分布 ---
		.append("\n ------ 资产分布 ------ \n")
		.append("    ").append("initMoney").append(":").append("'").append(initMoney).append("'").append(", \n")
		.append("    ").append("fundsBalance").append(":").append("'").append(fundsBalance).append("'").append(", \n")
		.append("    ").append("marketValue").append(":").append("'").append(marketValue).append("'").append(", \n")
		.append("    ").append("stockNumber").append(":").append("'").append(stockNumber).append("'").append(", \n")
		.append("    ").append("costPrice").append(":").append("'").append(costPrice).append("'").append(", \n")
		.append("    ").append("newPrice").append(":").append("'").append(newPrice).append("'").append(", \n")
		
		// --- 交易频率 ---
		.append("\n ------ 交易频率 ------ \n")
		.append("    ").append("dealDetailList").append(":").append("'").append(dealDetailList).append("'").append(", \n")
		.append("    ").append("dealNumber").append(":").append("'").append(dealNumber).append("'").append(", \n")
		.append("    ").append("buyNumber").append(":").append("'").append(buyNumber).append("'").append(", \n")
		.append("    ").append("sellNumber").append(":").append("'").append(sellNumber).append("'").append(", \n")
		.append("    ").append("avgBuyAndSellInterval").append(":").append("'").append(avgBuyAndSellInterval).append("'").append(", \n")
		.append("    ").append("maxBuyAndSellInterval").append(":").append("'").append(maxBuyAndSellInterval).append("'").append(", \n")
		.append("    ").append("minBuyAndSellInterval").append(":").append("'").append(minBuyAndSellInterval).append("'").append(", \n")
		
		// --- 交易周期 ---
		.append("\n ------ 交易周期 ------ \n")
		.append("    ").append("cyclePLDetailList").append(":").append("'").append(cyclePLDetailList).append("'").append(", \n")
		.append("    ").append("cycleNumber").append(":").append("'").append(cycleNumber).append("'").append(", \n")
		.append("    ").append("winNumber").append(":").append("'").append(winNumber).append("'").append(", \n")
		.append("    ").append("lossNumber").append(":").append("'").append(lossNumber).append("'").append(", \n")
		.append("    ").append("avgCycleInterval").append(":").append("'").append(avgCycleInterval).append("'").append(", \n")
		.append("    ").append("maxCycleInterval").append(":").append("'").append(maxCycleInterval).append("'").append(", \n")
		.append("    ").append("minCycleInterval").append(":").append("'").append(minCycleInterval).append("'").append(", \n")
		// --- 周期盈亏 ---
		.append("\n ------ 周期盈亏 ------ \n")
		.append("    ").append("avgProfit").append(":").append("'").append(avgProfit).append("'").append(", \n")
		.append("    ").append("avgLoss").append(":").append("'").append(avgLoss).append("'").append(", \n")
		.append("    ").append("maxProfit").append(":").append("'").append(maxProfit).append("'").append(", \n")
		.append("    ").append("maxLoss").append(":").append("'").append(maxLoss).append("'").append(", \n")
		.append("    ").append("minProfit").append(":").append("'").append(minProfit).append("'").append(", \n")
		.append("    ").append("minLoss").append(":").append("'").append(minLoss).append("'").append(", \n")
		
		// --- 最后一个交易信号 ---
		.append("    ").append("lastDealSignal").append(":").append("'").append(lastDealSignal).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}

	// --- get and set method ---
	
	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public BigDecimal getTotalAsset() {
		return totalAsset;
	}

	public void setTotalAsset(BigDecimal totalAsset) {
		this.totalAsset = totalAsset;
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

	public BigDecimal getWinRate() {
		return winRate;
	}

	public void setWinRate(BigDecimal winRate) {
		this.winRate = winRate;
	}

	public BigDecimal getInitMoney() {
		return initMoney;
	}

	public void setInitMoney(BigDecimal initMoney) {
		this.initMoney = initMoney;
	}

	public BigDecimal getFundsBalance() {
		return fundsBalance;
	}

	public void setFundsBalance(BigDecimal fundsBalance) {
		this.fundsBalance = fundsBalance;
	}

	public BigDecimal getMarketValue() {
		return marketValue;
	}

	public void setMarketValue(BigDecimal marketValue) {
		this.marketValue = marketValue;
	}

	public Long getStockNumber() {
		return stockNumber;
	}

	public void setStockNumber(Long stockNumber) {
		this.stockNumber = stockNumber;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(BigDecimal costPrice) {
		this.costPrice = costPrice;
	}

	public BigDecimal getNewPrice() {
		return newPrice;
	}

	public void setNewPrice(BigDecimal newPrice) {
		this.newPrice = newPrice;
	}

	public List<String> getDealDetailList() {
		return dealDetailList;
	}

	public void setDealDetailList(List<String> dealDetailList) {
		this.dealDetailList = dealDetailList;
	}

	public Integer getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(Integer dealNumber) {
		this.dealNumber = dealNumber;
	}

	public Integer getBuyNumber() {
		return buyNumber;
	}

	public void setBuyNumber(Integer buyNumber) {
		this.buyNumber = buyNumber;
	}

	public Integer getSellNumber() {
		return sellNumber;
	}

	public void setSellNumber(Integer sellNumber) {
		this.sellNumber = sellNumber;
	}

	public BigDecimal getAvgBuyAndSellInterval() {
		return avgBuyAndSellInterval;
	}

	public void setAvgBuyAndSellInterval(BigDecimal avgBuyAndSellInterval) {
		this.avgBuyAndSellInterval = avgBuyAndSellInterval;
	}

	public BigDecimal getMaxBuyAndSellInterval() {
		return maxBuyAndSellInterval;
	}

	public void setMaxBuyAndSellInterval(BigDecimal maxBuyAndSellInterval) {
		this.maxBuyAndSellInterval = maxBuyAndSellInterval;
	}

	public BigDecimal getMinBuyAndSellInterval() {
		return minBuyAndSellInterval;
	}

	public void setMinBuyAndSellInterval(BigDecimal minBuyAndSellInterval) {
		this.minBuyAndSellInterval = minBuyAndSellInterval;
	}

	public List<BigDecimal> getCyclePLDetailList() {
		return cyclePLDetailList;
	}

	public void setCyclePLDetailList(List<BigDecimal> cyclePLDetailList) {
		this.cyclePLDetailList = cyclePLDetailList;
	}

	public Integer getCycleNumber() {
		return cycleNumber;
	}

	public void setCycleNumber(Integer cycleNumber) {
		this.cycleNumber = cycleNumber;
	}

	public Integer getWinNumber() {
		return winNumber;
	}

	public void setWinNumber(Integer winNumber) {
		this.winNumber = winNumber;
	}

	public Integer getLossNumber() {
		return lossNumber;
	}

	public void setLossNumber(Integer lossNumber) {
		this.lossNumber = lossNumber;
	}

	public BigDecimal getAvgCycleInterval() {
		return avgCycleInterval;
	}

	public void setAvgCycleInterval(BigDecimal avgCycleInterval) {
		this.avgCycleInterval = avgCycleInterval;
	}

	public BigDecimal getMaxCycleInterval() {
		return maxCycleInterval;
	}

	public void setMaxCycleInterval(BigDecimal maxCycleInterval) {
		this.maxCycleInterval = maxCycleInterval;
	}

	public BigDecimal getMinCycleInterval() {
		return minCycleInterval;
	}

	public void setMinCycleInterval(BigDecimal minCycleInterval) {
		this.minCycleInterval = minCycleInterval;
	}

	public BigDecimal getAvgProfit() {
		return avgProfit;
	}

	public void setAvgProfit(BigDecimal avgProfit) {
		this.avgProfit = avgProfit;
	}

	public BigDecimal getAvgLoss() {
		return avgLoss;
	}

	public void setAvgLoss(BigDecimal avgLoss) {
		this.avgLoss = avgLoss;
	}

	public BigDecimal getMaxProfit() {
		return maxProfit;
	}

	public void setMaxProfit(BigDecimal maxProfit) {
		this.maxProfit = maxProfit;
	}

	public BigDecimal getMaxLoss() {
		return maxLoss;
	}

	public void setMaxLoss(BigDecimal maxLoss) {
		this.maxLoss = maxLoss;
	}

	public BigDecimal getMinProfit() {
		return minProfit;
	}

	public void setMinProfit(BigDecimal minProfit) {
		this.minProfit = minProfit;
	}

	public BigDecimal getMinLoss() {
		return minLoss;
	}

	public void setMinLoss(BigDecimal minLoss) {
		this.minLoss = minLoss;
	}

	public FractalDealSignalBean getLastDealSignal() {
		return lastDealSignal;
	}

	public void setLastDealSignal(FractalDealSignalBean lastDealSignal) {
		this.lastDealSignal = lastDealSignal;
	}
}
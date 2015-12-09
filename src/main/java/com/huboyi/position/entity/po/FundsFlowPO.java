package com.huboyi.position.entity.po;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

import com.huboyi.position.entity.po.OrderInfoPO.Trade;

/**
 * 资金流水PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class FundsFlowPO implements Serializable {

	private static final long serialVersionUID = -2803161677982469792L;
	
	/** id */
	@Id
	private long id;
	/** 合同编号。*/
	private String contractCode;
	/** 币种。*/
	private String currency = "人民币";
	
	// --- 
	/** 证券代码。*/
	private String stockCode;
	/** 证券名称。*/
	private String stockName;
	/** 成交日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long tradeDate;
	/** 成交价格。*/
	private BigDecimal tradePrice;
	/** 成交数量。*/
	private Long tradeNumber;
	/**
	 * 成交金额。
	 * 买入时：成交金额 ==（交易总金额 + 手续费 + 印花税 + 过户费 + 结算费）。
	 * 卖出时：成交金额 ==（交易总金额 - 手续费 - 印花税 - 过户费 - 结算费）。
	 */
	private BigDecimal tradeMoney;
	/** 资金余额。*/
	private BigDecimal fundsBalance;
	
	// ---
	/** 业务类型（在数据库中实际记录的值，主要用于查询）。*/
	private int businessType;
	/** 业务名称（不在数据库中记录该值，主要用于显示）。*/
	private String businessName;
	
	// --- 
	/** 手续费。*/
	private BigDecimal charges;
	/** 印花税。*/
	private BigDecimal stampDuty;
	/** 过户费。*/
	private BigDecimal transferFee;
	/** 结算费（不购买B股不用计算）。*/
	private BigDecimal clearingFee;
	
	// --- 
	/** 股东代码。*/
	private String stockholder = "672288";
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		.append("    ").append("id").append(":").append("'").append(id).append("'").append(", \n")
		.append("    ").append("contractCode").append(":").append("'").append(contractCode).append("'").append(", \n")
		.append("    ").append("currency").append(":").append("'").append(currency).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		.append("    ").append("tradeDate").append(":").append("'").append(tradeDate).append("'").append(", \n")
		.append("    ").append("tradePrice").append(":").append("'").append(tradePrice).append("'").append(", \n")
		.append("    ").append("tradeNumber").append(":").append("'").append(tradeNumber).append("'").append(", \n")
		.append("    ").append("tradeMoney").append(":").append("'").append(tradeMoney).append("'").append(", \n")
		.append("    ").append("fundsBalance").append(":").append("'").append(fundsBalance).append("'").append(", \n")
		
		// --- 
		.append("    ").append("businessType").append(":").append("'").append(businessType).append("'").append(", \n")
		.append("    ").append("businessName").append(":").append("'").append(businessName).append("'").append(", \n")
		
		// --- 
		.append("    ").append("charges").append(":").append("'").append(charges).append("'").append(", \n")
		.append("    ").append("stampDuty").append(":").append("'").append(stampDuty).append("'").append(", \n")
		.append("    ").append("transferFee").append(":").append("'").append(transferFee).append("'").append(", \n")
		.append("    ").append("clearingFee").append(":").append("'").append(clearingFee).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockholder").append(":").append("'").append(stockholder).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}
	
	/** 资金流水业务类型枚举。*/
	public enum Business {
		ROLL_IN(0, "银行转入"),
		ROLL_OUT(1, "资金转出"),
		
		STOCK_BUY(2, "证券买入"),
		STOCK_SELL(3, "证券卖出");
		
		private final int type;
		private final String name;
		private Business (int type, String name) {
			this.type = type;
			this.name = name;
		}
		public int getType () {
			return type;
		}
		public String getName() {
			return name;
		}
	}
	
	// --- get method and set method ---
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContractCode() {
		return contractCode;
	}

	public void setContractCode(String contractCode) {
		this.contractCode = contractCode;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
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

	public Long getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(Long tradeDate) {
		this.tradeDate = tradeDate;
	}

	public BigDecimal getTradePrice() {
		return tradePrice;
	}

	public void setTradePrice(BigDecimal tradePrice) {
		this.tradePrice = tradePrice;
	}

	public Long getTradeNumber() {
		return tradeNumber;
	}

	public void setTradeNumber(Long tradeNumber) {
		this.tradeNumber = tradeNumber;
	}

	public BigDecimal getTradeMoney() {
		return tradeMoney;
	}

	public void setTradeMoney(BigDecimal tradeMoney) {
		this.tradeMoney = tradeMoney;
	}

	public BigDecimal getFundsBalance() {
		return fundsBalance;
	}

	public void setFundsBalance(BigDecimal fundsBalance) {
		this.fundsBalance = fundsBalance;
	}

	public int getBusinessType() {
		return businessType;
	}

	public void setBusinessType(int businessType) {
		this.businessType = businessType;
		
		for (Trade e : Trade.values()) {
			if (businessType == e.getType()) {
				setBusinessName(e.getName());
				break;
			}
		}
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public BigDecimal getCharges() {
		return charges;
	}

	public void setCharges(BigDecimal charges) {
		this.charges = charges;
	}

	public BigDecimal getStampDuty() {
		return stampDuty;
	}

	public void setStampDuty(BigDecimal stampDuty) {
		this.stampDuty = stampDuty;
	}

	public BigDecimal getTransferFee() {
		return transferFee;
	}

	public void setTransferFee(BigDecimal transferFee) {
		this.transferFee = transferFee;
	}

	public BigDecimal getClearingFee() {
		return clearingFee;
	}

	public void setClearingFee(BigDecimal clearingFee) {
		this.clearingFee = clearingFee;
	}

	public String getStockholder() {
		return stockholder;
	}

	public void setStockholder(String stockholder) {
		this.stockholder = stockholder;
	}
}
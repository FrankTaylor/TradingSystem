package com.huboyi.trader.entity.po;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

import com.huboyi.trader.entity.constant.BusinessType;
import com.huboyi.trader.entity.constant.CurrencyType;

/**
 * 资金流水 PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class FundsFlowPO implements Serializable {
	
	private static final long serialVersionUID = 8773254819660337545L;
	
	/** id */
	@Id
	private Long id;
	/** 资金流水编号。*/
	private String fundsFlowCode;
	/** 交易单编号。*/
	private String dealOrderCode;
	
	// ---
	/** 业务类型（在数据库中实际记录的值，主要用于查询）。*/
	private Integer businessType;
	/** 业务类型说明（不在数据库中记录该值，主要用于显示）。*/
	private String businessTypeDesc;
	
	// ---
	/** 币种类型（在数据库中实际记录的值，主要用于查询）。*/
	private Integer currencyType;
	/** 币种类型说明（不在数据库中记录该值，主要用于显示）。*/
	private String currencyTypeDesc;
	
	// --- 
	/** 证券代码。*/
	private String stockCode;
	/** 证券名称。*/
	private String stockName;
	
	// ---
	/** 成交日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long tradeDate;
	/** 成交价格。*/
	private BigDecimal tradePrice;
	/** 成交数量。*/
	private Long tradeVolume;
	
	// --- 
	/** 手续费。*/
	private BigDecimal charges;
	/** 印花税。*/
	private BigDecimal stampDuty;
	/** 过户费。*/
	private BigDecimal transferFee;
	/** 结算费（不购买 B 股不用计算）。*/
	private BigDecimal clearingFee;
	
	// ---
	/**
	 * 成交金额。
	 * 买入时：成交金额 ==（交易总金额 + 手续费 + 印花税 + 过户费 + 结算费）。
	 * 卖出时：成交金额 ==（交易总金额 - 手续费 - 印花税 - 过户费 - 结算费）。
	 */
	private BigDecimal turnover;
	/** 资金余额。*/
	private BigDecimal fundsBalance;
	
	// --- 
	/** 股东代码。*/
	private String stockholder = "672288";
	
	// ---
	/** 创建时间。*/
	private Timestamp createTime;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		.append("    ").append("id").append(":").append("'").append(id).append("'").append(", \n")
		.append("    ").append("fundsFlowCode").append(":").append("'").append(fundsFlowCode).append("'").append(", \n")
		.append("    ").append("dealOrderCode").append(":").append("'").append(dealOrderCode).append("'").append(", \n")
		
		// ---
		.append("    ").append("businessType").append(":").append("'").append(businessType).append("'").append(", \n")
		.append("    ").append("businessTypeDesc").append(":").append("'").append(businessTypeDesc).append("'").append(", \n")
		
		// ---
		.append("    ").append("currencyType").append(":").append("'").append(currencyType).append("'").append(", \n")
		.append("    ").append("currencyTypeDesc").append(":").append("'").append(currencyTypeDesc).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		
		// ---
		.append("    ").append("tradeDate").append(":").append("'").append(tradeDate).append("'").append(", \n")
		.append("    ").append("tradePrice").append(":").append("'").append(tradePrice).append("'").append(", \n")
		.append("    ").append("tradeVolume").append(":").append("'").append(tradeVolume).append("'").append(", \n")
		
		// --- 
		.append("    ").append("charges").append(":").append("'").append(charges).append("'").append(", \n")
		.append("    ").append("stampDuty").append(":").append("'").append(stampDuty).append("'").append(", \n")
		.append("    ").append("transferFee").append(":").append("'").append(transferFee).append("'").append(", \n")
		.append("    ").append("clearingFee").append(":").append("'").append(clearingFee).append("'").append(", \n")
		
		// --- 
		.append("    ").append("turnover").append(":").append("'").append(turnover).append("'").append(", \n")
		.append("    ").append("fundsBalance").append(":").append("'").append(fundsBalance).append("'").append(", \n")

		// --- 
		.append("    ").append("stockholder").append(":").append("'").append(stockholder).append("'").append(", \n")
		
		// --- 
		.append("    ").append("createTime").append(":").append("'").append(createTime).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}
	
	// --- get method and set method ---
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFundsFlowCode() {
		return fundsFlowCode;
	}

	public void setFundsFlowCode(String fundsFlowCode) {
		this.fundsFlowCode = fundsFlowCode;
	}

	public String getDealOrderCode() {
		return dealOrderCode;
	}

	public void setDealOrderCode(String dealOrderCode) {
		this.dealOrderCode = dealOrderCode;
	}

	public BusinessType getBusinessType() {
		if (this.businessType != null) {
			for (BusinessType e : BusinessType.values()) {
				if (this.businessType == e.getType()) {
					return e;
				}
			}
		}
		
		return null;
	}

	public void setBusinessType(Integer businessType) {
		this.businessType = businessType;
		for (BusinessType e : BusinessType.values()) {
			if (this.businessType == e.getType()) {
				setBusinessTypeDesc(e.getDesc());
				break;
			}
		}
	}

	public String getBusinessTypeDesc() {
		return businessTypeDesc;
	}

	public void setBusinessTypeDesc(String businessTypeDesc) {
		this.businessTypeDesc = businessTypeDesc;
	}

	public CurrencyType getCurrencyType() {
		if (this.currencyType != null) {
			for (CurrencyType e : CurrencyType.values()) {
				if (this.currencyType == e.getType()) {
					return e;
				}
			}
		}
		
		return null;
	}

	public void setCurrencyType(Integer currencyType) {
		this.currencyType = currencyType;
		for (CurrencyType e : CurrencyType.values()) {
			if (this.currencyType == e.getType()) {
				setCurrencyTypeDesc(e.getDesc());
				break;
			}
		}
	}

	public String getCurrencyTypeDesc() {
		return currencyTypeDesc;
	}

	public void setCurrencyTypeDesc(String currencyTypeDesc) {
		this.currencyTypeDesc = currencyTypeDesc;
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

	public Long getTradeVolume() {
		return tradeVolume;
	}

	public void setTradeVolume(Long tradeVolume) {
		this.tradeVolume = tradeVolume;
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

	public BigDecimal getTurnover() {
		return turnover;
	}

	public void setTurnover(BigDecimal turnover) {
		this.turnover = turnover;
	}

	public BigDecimal getFundsBalance() {
		return fundsBalance;
	}

	public void setFundsBalance(BigDecimal fundsBalance) {
		this.fundsBalance = fundsBalance;
	}

	public String getStockholder() {
		return stockholder;
	}

	public void setStockholder(String stockholder) {
		this.stockholder = stockholder;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
}
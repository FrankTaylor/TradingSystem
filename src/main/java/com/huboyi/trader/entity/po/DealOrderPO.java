package com.huboyi.trader.entity.po;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

import com.huboyi.trader.entity.constant.DealType;

/**
 * 交易单 PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class DealOrderPO implements Serializable {

	private static final long serialVersionUID = -6603096397378109107L;
	
	/** id */
	@Id
	private long id;
	/** 交易单编号。*/
	private String dealOrderCode;
	/** 委托单编号。*/
	private String entrustOrderCode;
	
	// --- 
	/** 证券代码。*/
	private String stockCode;
	/** 证券名称。*/
	private String stockName;
	
	// ---
	/** 交易类型（在数据库中实际记录的值，主要用于查询）。*/
	private Integer dealType;
	/** 交易类型说明（不在数据库中记录该值，主要用于显示）。*/
	private String dealTypeDesc;
	
	// ---
	/** 成交日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long tradeDate;
	/** 成交价格。*/
	private BigDecimal tradePrice;
	/** 成交数量。*/
	private Long tradeVolume;
	/** 成交额（公式：成交额 = 成交价格 * 成交数量，这里不考虑手续费问题）。*/
	private Long turnover;
	
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
		.append("    ").append("dealOrderCode").append(":").append("'").append(dealOrderCode).append("'").append(", \n")
		.append("    ").append("entrustOrderCode").append(":").append("'").append(entrustOrderCode).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		
		// ---
		.append("    ").append("dealType").append(":").append("'").append(dealType).append("'").append(", \n")
		.append("    ").append("dealTypeDesc").append(":").append("'").append(dealTypeDesc).append("'").append(", \n")
		
		// ---
		.append("    ").append("tradeDate").append(":").append("'").append(tradeDate).append("'").append(", \n")
		.append("    ").append("tradePrice").append(":").append("'").append(tradePrice).append("'").append(", \n")
		.append("    ").append("tradeVolume").append(":").append("'").append(tradeVolume).append("'").append(", \n")
		.append("    ").append("turnover").append(":").append("'").append(turnover).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockholder").append(":").append("'").append(stockholder).append("'").append(", \n")
		
		// --- 
		.append("    ").append("createTime").append(":").append("'").append(createTime).append("'").append(", \n");
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

	public String getDealOrderCode() {
		return dealOrderCode;
	}

	public void setDealOrderCode(String dealOrderCode) {
		this.dealOrderCode = dealOrderCode;
	}

	public String getEntrustOrderCode() {
		return entrustOrderCode;
	}

	public void setEntrustOrderCode(String entrustOrderCode) {
		this.entrustOrderCode = entrustOrderCode;
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

	public DealType getDealType() {
		if (this.dealType != null) {
			for (DealType e : DealType.values()) {
				if (this.dealType == e.getType()) {
					return e;
				}
			}
		}
		
		return null;
	}

	public void setDealType(Integer dealType) {
		this.dealType = dealType;
		for (DealType e : DealType.values()) {
			if (this.dealType == e.getType()) {
				setDealTypeDesc(e.getDesc());
				break;
			}
		}
	}

	public String getDealTypeDesc() {
		return dealTypeDesc;
	}

	public void setDealTypeDesc(String dealTypeDesc) {
		this.dealTypeDesc = dealTypeDesc;
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

	public Long getTurnover() {
		return turnover;
	}

	public void setTurnover(Long turnover) {
		this.turnover = turnover;
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
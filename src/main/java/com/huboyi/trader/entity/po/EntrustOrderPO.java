package com.huboyi.trader.entity.po;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

import com.huboyi.trader.entity.constant.DealStatus;
import com.huboyi.trader.entity.constant.DealType;
import com.huboyi.trader.entity.constant.QuoteType;

/**
 * 委托单 PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class EntrustOrderPO implements Serializable {

	private static final long serialVersionUID = -1730021379522889743L;
	
	/** id */
	@Id
	private long id;
	/** 委托单编号。*/
	private String entrustOrderCode;
	/** 被撤销委托单编号。*/
	private String cancelEntrustOrderCode;
	
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
	/** 交易状态（在数据库中实际记录的值，主要用于查询）。*/
	private Integer dealStatus;
	/** 交易理状态说明（不在数据库中记录该值，主要用于显示）。*/
	private String dealStatusDesc;
	/** 报价方式（在数据库中实际记录的值，主要用于查询）。*/
	private Integer quoteType;
	/** 报价方式说明（不在数据库中记录该值，主要用于显示）。*/
	private String quoteTypeDesc;
	
	// ---
	/** 委托日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long entrustDate;
	/** 委托价格。*/
	private BigDecimal entrustPrice;
	/** 委托数量。*/
	private Long entrustVolume;
	/** 成交数量。*/
	private Long tradeVolume;
	
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
		.append("    ").append("entrustOrderCode").append(":").append("'").append(entrustOrderCode).append("'").append(", \n")
		.append("    ").append("cancelEntrustOrderCode").append(":").append("'").append(cancelEntrustOrderCode).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		
		// ---
		.append("    ").append("dealType").append(":").append("'").append(dealType).append("'").append(", \n")
		.append("    ").append("dealTypeDesc").append(":").append("'").append(dealTypeDesc).append("'").append(", \n")
		.append("    ").append("dealStatus").append(":").append("'").append(dealStatus).append("'").append(", \n")
		.append("    ").append("dealStatusDesc").append(":").append("'").append(dealStatusDesc).append("'").append(", \n")
		.append("    ").append("quoteType").append(":").append("'").append(quoteType).append("'").append(", \n")
		.append("    ").append("quoteTypeDesc").append(":").append("'").append(quoteTypeDesc).append("'").append(", \n")
		
		// ---
		.append("    ").append("entrustDate").append(":").append("'").append(entrustDate).append("'").append(", \n")
		.append("    ").append("entrustPrice").append(":").append("'").append(entrustPrice).append("'").append(", \n")
		.append("    ").append("entrustVolume").append(":").append("'").append(entrustVolume).append("'").append(", \n")
		.append("    ").append("tradeVolume").append(":").append("'").append(tradeVolume).append("'").append(", \n")
		
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

	public String getEntrustOrderCode() {
		return entrustOrderCode;
	}

	public void setEntrustOrderCode(String entrustOrderCode) {
		this.entrustOrderCode = entrustOrderCode;
	}

	public String getCancelEntrustOrderCode() {
		return cancelEntrustOrderCode;
	}

	public void setCancelEntrustOrderCode(String cancelEntrustOrderCode) {
		this.cancelEntrustOrderCode = cancelEntrustOrderCode;
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

	public DealStatus getDealStatus() {
		if (this.dealStatus != null) {
			for (DealStatus e : DealStatus.values()) {
				if (this.dealStatus == e.getType()) {
					return e;
				}
			}
		}
		
		return null;
	}

	public void setDealStatus(Integer dealStatus) {
		this.dealStatus = dealStatus;
		for (DealStatus e : DealStatus.values()) {
			if (this.dealStatus == e.getType()) {
				setDealStatusDesc(e.getDesc());
				break;
			}
		}
	}

	public String getDealStatusDesc() {
		return dealStatusDesc;
	}

	public void setDealStatusDesc(String dealStatusDesc) {
		this.dealStatusDesc = dealStatusDesc;
	}

	public QuoteType getQuoteType() {
		if (this.quoteType != null) {
			for (QuoteType e : QuoteType.values()) {
				if (this.quoteType == e.getType()) {
					return e;
				}
			}
		}
		
		return null;
	}

	public void setQuoteType(Integer quoteType) {
		this.quoteType = quoteType;
		for (QuoteType e : QuoteType.values()) {
			if (this.quoteType == e.getType()) {
				setQuoteTypeDesc(e.getDesc());
				break;
			}
		}
	}

	public String getQuoteTypeDesc() {
		return quoteTypeDesc;
	}

	public void setQuoteTypeDesc(String quoteTypeDesc) {
		this.quoteTypeDesc = quoteTypeDesc;
	}

	public Long getEntrustDate() {
		return entrustDate;
	}

	public void setEntrustDate(Long entrustDate) {
		this.entrustDate = entrustDate;
	}

	public BigDecimal getEntrustPrice() {
		return entrustPrice;
	}

	public void setEntrustPrice(BigDecimal entrustPrice) {
		this.entrustPrice = entrustPrice;
	}

	public Long getEntrustVolume() {
		return entrustVolume;
	}

	public void setEntrustVolume(Long entrustVolume) {
		this.entrustVolume = entrustVolume;
	}

	public Long getTradeVolume() {
		return tradeVolume;
	}

	public void setTradeVolume(Long tradeVolume) {
		this.tradeVolume = tradeVolume;
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
package com.huboyi.trader.entity.po;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

/**
 * 报单信息PO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class OrderInfoPO implements Serializable {
	
	private static final long serialVersionUID = -4059114031070394655L;
	
	/** id */
	@Id
	private long id;
	/** 合同编号。*/
	private String contractCode;
	
	// --- 
	/** 证券代码。*/
	private String stockCode;
	/** 证券名称。*/
	private String stockName;
	/** 买卖类型（在数据库中实际记录的值，主要用于查询）。*/
	private Integer tradeType;
	
	
	
	
	
	/** 成交日期（格式：yyyyMMddhhmmssSSS）。*/
	private Long tradeDate;
	
	/** 买卖类型说明（不在数据库中记录该值，主要用于显示）。*/
	private String tradeTypeDesc;
	/** 处理状态（在数据库中实际记录的值，主要用于查询）。*/
	private Integer dealStatus;
	/** 处理状态说明（不在数据库中记录该值，主要用于显示）。*/
	private String dealStatusDesc;
	
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
		.append("    ").append("contractCode").append(":").append("'").append(contractCode).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockCode").append(":").append("'").append(stockCode).append("'").append(", \n")
		.append("    ").append("stockName").append(":").append("'").append(stockName).append("'").append(", \n")
		.append("    ").append("tradeDate").append(":").append("'").append(tradeDate).append("'").append(", \n")
		.append("    ").append("tradeType").append(":").append("'").append(tradeType).append("'").append(", \n")
		.append("    ").append("tradeTypeDesc").append(":").append("'").append(tradeTypeDesc).append("'").append(", \n")
		.append("    ").append("dealStatus").append(":").append("'").append(dealStatus).append("'").append(", \n")
		.append("    ").append("dealStatusDesc").append(":").append("'").append(dealStatusDesc).append("'").append(", \n")
		.append("    ").append("tradePrice").append(":").append("'").append(tradePrice).append("'").append(", \n")
		.append("    ").append("tradeNumber").append(":").append("'").append(tradeNumber).append("'").append(", \n")
		.append("    ").append("tradeMoney").append(":").append("'").append(tradeMoney).append("'").append(", \n")
		
		// --- 
		.append("    ").append("stockholder").append(":").append("'").append(stockholder).append("'").append(", \n")
		
		// --- 
		.append("    ").append("createTime").append(":").append("'").append(createTime).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}
	
	/** 订单信息中的买卖标志枚举类。*/
	public enum TradeTypeEnum {
		STOCK_BUY(1, "证券买入"),
		STOCK_SELL(2, "证券卖出");
		
		private final int type;
		private final String desc;
		private TradeTypeEnum(int type, String desc) {
			this.type = type;
			this.desc = desc;
		}
		public int getType () { return type; }
		public String getDesc() { return desc; }
	}
	
	public enum DealTypeEnum {
		TRADE_SUCCESS(1, "交易成功"),
		TRADE_FAIL(2, "交易失败");
		
		private final int type;
		private final String desc;
		private DealTypeEnum(int type, String desc) {
			this.type = type;
			this.desc = desc;
		}
		public int getType() { return type; }
		public String getDesc() { return desc; }
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

	public int getTradeType() {
		return tradeType;
	}

	public void setTradeType(int tradeType) {
		this.tradeType = tradeType;
		
		for (TradeTypeEnum e : TradeTypeEnum.values()) {
			if (tradeType == e.getType()) {
				setTradeName(e.getName());
				break;
			}
		}
	}
	
	public int getTradeStatus() {
		return tradeStatus;
	}
	
	public void setTradeStatus(int tradeStatus) {
		this.tradeStatus = tradeStatus;
		
		for (TradeStatusEnum e : TradeStatusEnum.values()) {
			if (tradeType == e.getType()) {
				setTradeName(e.getName());
				break;
			}
		}
	}
	
	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
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

	public String getStockholder() {
		return stockholder;
	}

	public void setStockholder(String stockholder) {
		this.stockholder = stockholder;
	}
}
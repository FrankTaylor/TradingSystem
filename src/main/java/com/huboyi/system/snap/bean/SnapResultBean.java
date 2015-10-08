package com.huboyi.system.snap.bean;

import com.huboyi.system.constant.OrderInfoTradeFlag;

/**
 * 信号捕捉结果信息。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/4/20
 * @version 1.0
 */
public class SnapResultBean {
	
	/** 订单信息中的买卖标志枚举类。*/
	private OrderInfoTradeFlag orderInfoTradeFlag;
	/** 证券代码。*/
	private String stockCode;
	/** 信号日期。*/
	private Long signalDate;
	/** 信号类型。*/
	private String signalType;
	/** 信号名称。*/
	private String signalName;
	/** 交易数量。*/
	private Long tradeNumber;
	
	// --- get and set method ---
	
	public OrderInfoTradeFlag getOrderInfoTradeFlag() {
		return orderInfoTradeFlag;
	}
	
	public void setOrderInfoTradeFlag(OrderInfoTradeFlag orderInfoTradeFlag) {
		this.orderInfoTradeFlag = orderInfoTradeFlag;
	}
	
	public String getStockCode() {
		return stockCode;
	}
	
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	
	public Long getSignalDate() {
		return signalDate;
	}
	
	public void setSignalDate(Long signalDate) {
		this.signalDate = signalDate;
	}
	
	public String getSignalType() {
		return signalType;
	}
	
	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}
	
	public String getSignalName() {
		return signalName;
	}
	
	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}
	
	public Long getTradeNumber() {
		return tradeNumber;
	}
	
	public void setTradeNumber(Long tradeNumber) {
		this.tradeNumber = tradeNumber;
	}
}
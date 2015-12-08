package com.huboyi.position.constant;

/**
 * 订单信息中的买卖标志枚举类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public enum OrderInfoTradeFlag {
	STOCK_BUY("证券买入"),
	STOCK_SELL("证券卖出");
	
	private final String type;
	private OrderInfoTradeFlag (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
}
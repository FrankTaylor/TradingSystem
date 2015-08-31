package com.huboyi.system.constant;

/**
 * 订单信息中的买卖标志枚举类。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/11
 * @version 1.0
 */
public enum OrderInfoTradeFlagEnum {
	STOCK_BUY("证券买入"),
	STOCK_SELL("证券卖出");
	
	private final String type;
	private OrderInfoTradeFlagEnum (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
}
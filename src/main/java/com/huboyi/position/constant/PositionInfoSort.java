package com.huboyi.position.constant;

/**
 * 持仓信息中的排序标志枚举类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public enum PositionInfoSort {
	STOCK_BUY("证券买入"),
	STOCK_SELL("证券卖出");
	
	private final String type;
	private PositionInfoSort (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
}
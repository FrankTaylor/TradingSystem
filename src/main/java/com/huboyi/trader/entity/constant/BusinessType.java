package com.huboyi.trader.entity.constant;

/**
 * 资金流水业务类型枚举。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public enum BusinessType {
	
	ROLL_IN(1, "银行转入"),
	ROLL_OUT(2, "资金转出"),
	
	STOCK_BUY(3, "证券买入"),
	STOCK_SELL(4, "证券卖出");
	
	private final int type;
	private final String desc;
	private BusinessType(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}
	
	public int getType() { return type; }
	public String getDesc() { return desc; }
}
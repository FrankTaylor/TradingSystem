package com.huboyi.trader.entity.constant;

/**
 * 交易类型枚举（目前该交易测试系统仅支持 “买入” 和 “卖出”）。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public enum DealType {
	
	BUY(1, "买入"),
	CANCEL_BUY(2, "撤买入"),
	SELL(3, "卖出"),
	CANCEL_SELL(4, "撤卖出");
	
	private final int type;
	private final String desc;
	private DealType(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}
	
	public int getType() { return type; }
	public String getDesc() { return desc; }
}
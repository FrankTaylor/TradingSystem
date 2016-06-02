package com.huboyi.trader.entity.constant;

/**
 * 币种类型枚举。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 *
 */
public enum CurrencyType {
	
	CHINA(1, "人民币");
	
	private final int type;
	private final String desc;
	private CurrencyType(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}
	
	public int getType() { return type; }
	public String getDesc() { return desc; }
}
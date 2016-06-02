package com.huboyi.trader.entity.constant;

/**
 * 报价类型枚举（目前该交易测试系统仅支持 “限价”）。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public enum QuoteType {
	
	LIMIT_PRICE(1, "限价"),
	A(2, "对方最优价格"),
	B(3, "本方最优价格"),
	C(4, "即时成交剩余撤销"),
	D(5, "五档即成剩撤"),
	E(6, "全额成交或撤销");
	
	private final int type;
	private final String desc;
	private QuoteType(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}
	
	public int getType() { return type; }
	public String getDesc() { return desc; }
}
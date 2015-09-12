package com.huboyi.system.constant;


/**
 * 交易信号类型枚举。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/9/12
 * @version 1.0
 */
public enum DealSignalEnum {
	ONE_B("1B", "第一买点"),
	FIBO_B("fiboB", "斐波那契买点"),
	
	SELL_ONE_TENTH("sell one tenth", "卖出十分之一"),
	SELL_TWO_TENTH("sell two tenth", "卖出十分之二"),
	SELL_THREE_TENTH("sell three tenth", "卖出十分之三"),
	SELL_FOUR_TENTH("sell four tenth", "卖出十分之四"),
	SELL_FIVE_TENTH("sell five tenth", "卖出十分之五"),
	SELL_SIX_TENTH("sell six tenth", "卖出十分之六"),
	SELL_SEVEN_TENTH("sell seven tenth", "卖出十分之七"),
	SELL_EIGHT_TENTH("sell eight tenth", "卖出十分之八"),
	SELL_NINE_TENTH("sell nine tenth", "卖出十分之九"),
	SELL_ALL("sell all", "全部卖出");

	private final String type;
	private final String name;
	private DealSignalEnum (String type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public String getType () {
		return type;
	}
	
	public String getName () {
		return name;
	}
}
	
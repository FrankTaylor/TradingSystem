package com.huboyi.position.constant;


/**
 * 交易信号类型枚举。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public enum DealSignal {
	
	/** 多头开仓。*/
	BUY_TO_OPEN,
	/** 多头平仓。*/
	SELL_TO_CLOSE,
	
	/** 空头开仓。*/
	SELL_TO_OPEN,
	/** 空头平仓。*/
	BUY_TO_CLOSE;
	
	private DealSignal () {}
}
	
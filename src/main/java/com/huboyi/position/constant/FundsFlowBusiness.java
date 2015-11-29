package com.huboyi.position.constant;

/**
 * 资金流水业务类型枚举。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/11
 * @version 1.0
 */
public enum FundsFlowBusiness {
	ROLL_IN("银行转入"),
	ROLL_OUT("资金转出"),
	STOCK_BUY("证券买入"),
	STOCK_SELL("证券卖出");
	
	private final String type;
	private FundsFlowBusiness (String type) {
		this.type = type;
	}
	
	public String getType () {
		return type;
	}
}
	
package com.huboyi.trader.entity.constant;

/**
 * 交易状态枚举（目前该交易测试系统仅支持 “未成交” 和 “已成交”）。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public enum DealStatus {
	
	NOT_CLINCH_DEAL(1, "未成交"),
	CLINCH_DEAL(2, "已成交"),
	
	CANCEL_ORDER(3, "场内撤单"),
	CANCEL_ORDER_SUCCESS(4, "撤单成功");
	
	private final int type;
	private final String desc;
	private DealStatus(int type, String desc) {
		this.type = type;
		this.desc = desc;
	}
	
	public int getType() { return type; }
	public String getDesc() { return desc; }
}
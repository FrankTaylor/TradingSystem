package com.huboyi.system.bean;

import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.constant.DealSignalEnum;

/**
 * 用于记录交易信号的Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/12
 * @version 1.0
 */
public abstract class DealSignalBean {
	
	/** 发出交易信号的行情信息。*/
	private StockDataBean stockDataBean;
	/** 交易信号类型枚举。*/
	private DealSignalEnum type;
	
	/**
	 * 构造函数。
	 * 
	 * @param stockDataBean 作为交易信号的行情数据
	 * @param type 信号类型
	 */
	public DealSignalBean (StockDataBean stockDataBean, DealSignalEnum type) {
		this.stockDataBean = stockDataBean;
		this.type = type;
	}
	
	@Override
	public String toString () {
		StringBuilder builder = new StringBuilder();
		builder.append("{ \n")
		.append("    ").append("stockDataBean").append(":").append("'").append(stockDataBean).append("'").append(", \n")
		.append("    ").append("fractalDealSignal").append(":").append("'").append(type).append("'").append(", \n");
		builder.append("} \n");
		return builder.toString();
	}
	
	// --- get method and set method ---

	public StockDataBean getStockDataBean() {
		return stockDataBean;
	}

	public DealSignalBean setStockDataBean(StockDataBean stockDataBean) {
		this.stockDataBean = stockDataBean;
		return this;
	}

	public DealSignalEnum getType() {
		return type;
	}

	public DealSignalBean setType(DealSignalEnum type) {
		this.type = type;
		return this;
	}
}
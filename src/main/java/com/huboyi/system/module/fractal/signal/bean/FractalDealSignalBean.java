package com.huboyi.system.module.fractal.signal.bean;

import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.module.fractal.signal.constant.FractalDealSignalEnum;

/**
 * 分型战法中用于记录交易信号的Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/8
 * @version 1.0
 */
public class FractalDealSignalBean {
	
	/** 发出交易信号的行情信息。*/
	private StockDataBean stockDataBean;
	/** 信号类别。*/
	private FractalDealSignalEnum type;
	
	/**
	 * 构造函数。
	 * 
	 * @param stockDataBean 作为交易信号的行情数据
	 * @param type 信号类型
	 */
	public FractalDealSignalBean (StockDataBean stockDataBean, FractalDealSignalEnum type) {
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

	public FractalDealSignalBean setStockDataBean(StockDataBean stockDataBean) {
		this.stockDataBean = stockDataBean;
		return this;
	}

	public FractalDealSignalEnum getType() {
		return type;
	}

	public FractalDealSignalBean setType(FractalDealSignalEnum type) {
		this.type = type;
		return this;
	}
}
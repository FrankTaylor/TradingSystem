package com.huboyi.system.module.fractal.signal.bean;

import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.constant.DealSignalEnum;

/**
 * 分型战法中用于记录交易信号的Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/8
 * @version 1.0
 */
public class FractalDealSignalBean extends DealSignalBean {

	public FractalDealSignalBean(StockDataBean stockDataBean, DealSignalEnum type) {
		super(stockDataBean, type);
	}
}
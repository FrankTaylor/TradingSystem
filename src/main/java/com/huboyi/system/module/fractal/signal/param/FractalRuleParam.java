package com.huboyi.system.module.fractal.signal.param;

import java.math.BigDecimal;

/**
 * 顶底分型交易系统中交易规则的参数类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/11/10
 * @version 1.0
 */
public class FractalRuleParam {
	
	/** 形成底分型时，最右边K线的涨幅百分比。*/
	private BigDecimal upPercent = BigDecimal.valueOf(0.2);
	
	/** 形成顶分型时，最右边K线的跌幅百分比。*/
	private BigDecimal downPercent = BigDecimal.valueOf(0.6);
	
	public FractalRuleParam () {}
	
	/**
	 * 构造函数。
	 * 
	 * @param upPercent 形成底分型时，最右边K线的涨幅百分比
	 * @param downPercent 形成顶分型时，最右边K线的跌幅百分比
	 */
	public FractalRuleParam (BigDecimal upPercent, BigDecimal downPercent) {
		this.upPercent = downPercent;
		this.downPercent = downPercent;
	}
	
	// --- get method and set method ---
	
	public BigDecimal getUpPercent() {
		return upPercent;
	}

	public void setUpPercent(BigDecimal upPercent) {
		this.upPercent = upPercent;
	}

	public BigDecimal getDownPercent() {
		return downPercent;
	}

	public void setDownPercent(BigDecimal downPercent) {
		this.downPercent = downPercent;
	}
}
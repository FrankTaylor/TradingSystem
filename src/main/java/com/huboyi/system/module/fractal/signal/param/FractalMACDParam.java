package com.huboyi.system.module.fractal.signal.param;

/**
 * 顶底分型交易系统中MACD指标的参数类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/11/10
 * @version 1.0
 */
public class FractalMACDParam {
	
	/** 短周期。*/
	private final int shortCycle;
	/** 长周期。*/
	private final int longCycle;
	/** DEA周期。*/
	private final int deaCycle;
	
	/**
	 * 构造函数。
	 * 
	 * @param shortCycle 短周期
	 * @param longCycle 长周期
	 * @param deaCycle dea周期
	 */
	public FractalMACDParam (int shortCycle, int longCycle, int deaCycle) {
		this.shortCycle = shortCycle;
		this.longCycle = longCycle;
		this.deaCycle = deaCycle;
	}
	
	// --- get method ---
	
	public int getShortCycle() {
		return shortCycle;
	}

	public int getLongCycle() {
		return longCycle;
	}

	public int getDeaCycle() {
		return deaCycle;
	}
}
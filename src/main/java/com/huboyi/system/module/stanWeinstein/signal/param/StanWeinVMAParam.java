package com.huboyi.system.module.stanWeinstein.signal.param;

/**
 * StanWeinstein交易系统中成交量均线的参数类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/31
 * @version 1.0
 */
public class StanWeinVMAParam {
	
	/** 短周期。*/
	private final int shortCycle;
	/** 长周期。*/
	private final int longCycle;
	
	/**
	 * 构造函数。
	 * 
	 * @param shortCycle 短周期
	 * @param longCycle 长周期
	 */
	public StanWeinVMAParam (int shortCycle, int longCycle) {
		this.shortCycle = shortCycle;
		this.longCycle = longCycle;
	}
	
	// --- get method ---
	
	public int getShortCycle() {
		return shortCycle;
	}

	public int getLongCycle() {
		return longCycle;
	}
}
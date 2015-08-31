package com.huboyi.system.module.stanWeinstein.signal.param;

/**
 * StanWeinstein交易系统中交易规则的参数类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/11/01
 * @version 1.0
 */
public class StanWeinRuleParam {
	
	/** 长期均线走平验证周期。*/
	private int flatTestCycle = 30;
	/** 长期均线走平验证周期内满足条件的数量。*/
	private double flatTestCycleSuccessNums = 0.8;
	/** 长期均线走平验证周期内均线间价差范围。*/
	private double flatTestDiffRange = 0.005;
	
	/** K线突破长期走平均线时，K线涨幅的百分比。*/
	private double beyondPercent = 0.6;
	
	public StanWeinRuleParam () {}
	
	/**
	 * 构造函数。
	 * 
	 * @param flatTestCycle 长期均线走平验证周期
	 * @param flatTestCycleSuccessNums 长期均线走平验证周期内满足条件的数量
	 * @param flatTestDiffRange 长期均线走平验证周期内均线间价差范围
	 * @param beyondPercent K线突破长期走平均线时，K线涨幅的百分比
	 */
	public StanWeinRuleParam (int flatTestCycle, double flatTestCycleSuccessNums, 
			double flatTestDiffRange, double beyondPercent) {
		this.flatTestCycle = flatTestCycle;
		this.flatTestCycleSuccessNums = flatTestCycleSuccessNums;
		this.flatTestDiffRange = flatTestDiffRange;
		this.beyondPercent = beyondPercent;
	}
	
	// --- get method ---
	
	public int getFlatTestCycle() {
		return flatTestCycle;
	}

	public int getFlatTestCycleSuccessNums() {
		return (int)(flatTestCycle * flatTestCycleSuccessNums);
	}

	public double getFlatTestDiffRange() {
		return flatTestDiffRange;
	}

	public double getBeyondPercent() {
		return beyondPercent;
	}
}
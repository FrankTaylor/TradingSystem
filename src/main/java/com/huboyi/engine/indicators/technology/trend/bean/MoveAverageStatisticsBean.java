package com.huboyi.engine.indicators.technology.trend.bean;

import java.util.List;

import com.huboyi.engine.indicators.technology.constant.SingleMaPatternEnum;

/**
 * 普通平均线分析结果指标。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 上午11:25:57
 * @version 1.0
 */
public class MoveAverageStatisticsBean {
	
	/** 均线集合类。*/
	private List<MoveAverageBean> maList;
	/** 升高占比。*/
	private Double upRate;
	/** 下降占比。*/
	private Double downRate;
	/** 速度。*/
	private Double speed;
	/** 均线形态枚举。*/
	private SingleMaPatternEnum pattern;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("upRate = ").append(upRate).append("\n")
		.append("\t").append("downRate = ").append(downRate).append("\n")
		.append("\t").append("speed = ").append(speed).append("\n")
		.append("\t").append("pattern = ").append(pattern).append("\n")
		.append("]\n");
		return builder.toString();
	}
	
	// --- get method and set method ---
	
	public List<MoveAverageBean> getMaList() {
		return maList;
	}
	
	public MoveAverageStatisticsBean setMaList(List<MoveAverageBean> maList) {
		this.maList = maList;
		return this;
	}
	
	public MoveAverageStatisticsBean setUpRate(Double upRate) {
		this.upRate = upRate;
		return this;
	}
	
	public MoveAverageStatisticsBean setDownRate(Double downRate) {
		this.downRate = downRate;
		return this;
	}
	
	public Double getSpeed() {
		return speed;
	}
	
	public MoveAverageStatisticsBean setSpeed(Double speed) {
		this.speed = speed;
		return this;
	}
	
	public SingleMaPatternEnum getPattern() {
		return pattern;
	}
	
	public MoveAverageStatisticsBean setPattern(SingleMaPatternEnum pattern) {
		this.pattern = pattern;
		return this;
	}
	
}
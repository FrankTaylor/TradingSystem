package com.huboyi.engine.indicators.technology.pattern.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.huboyi.data.load.bean.StockDataBean;

/**
 * 行情中枢Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/28
 * @version 1.0
 */
public class PowerBean implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -8910513269592319057L;

	/**
	 * 用于标示中枢方向（中枢方向是指该中枢所在走势类型的方向）的枚举类。
	 * 
	 * @author FrankTaylor <mailto:franktaylor@163.com>
	 * @since 2014/10/28
	 * @version 1.0
	 */
	public static enum PowerType {
		/** 向上。*/
		UP,
		/** 向下。*/
		DOWN;
	}
	
	/** 中枢方向。*/
	private PowerType powerType;
	
	/** 该中枢的参照波段。*/
	private BandBean reference;
	
	/** 中枢包含的波段。*/
	private List<BandBean> bandList;
	
	/** 中枢的开始K线。*/
	private StockDataBean startKLine;
	
	/** 中枢的结束K线。*/
	private StockDataBean endKLine;
	
	/** 中枢的最高价。*/
	private BigDecimal high;
	
	/** 中枢的最低价。*/
	private BigDecimal low;
	
	/** 中枢中波段的最高价。*/
	private BigDecimal bandMaxHigh;
	
	/** 中枢中波段的最低价。*/
	private BigDecimal bandMaxLow;
	
	// --- 其他信息 ---
	/** 上一个中枢。*/
	private PowerBean prev;
	/** 下一个中枢。*/
	private PowerBean next;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("power_type = ").append(powerType).append("\n")
		.append("\t").append("reference = ").append(reference).append("\n")
		.append("\t").append("band_list = ").append(bandList).append("\n")
		.append("\t").append("start_k_line = ").append(startKLine).append("\n")
		.append("\t").append("end_k_line = ").append(endKLine).append("\n")
		.append("\t").append("high = ").append(high).append("\n")
		.append("\t").append("low = ").append(low).append("\n")
		.append("\t").append("bandMaxHigh = ").append(bandMaxHigh).append("\n")
		.append("\t").append("bandMaxLow = ").append(bandMaxLow).append("\n")
		.append("]\n");
		return builder.toString();
	}
	
	@Override  
    public Object clone() throws CloneNotSupportedException {  
        return super.clone();  
    }
	
	// --- get method and set method ---
	
	public PowerType getPowerType() {
		return powerType;
	}

	public void setPowerType(PowerType powerType) {
		this.powerType = powerType;
	}

	public BandBean getReference() {
		return reference;
	}

	public void setReference(BandBean reference) {
		this.reference = reference;
	}

	public List<BandBean> getBandList() {
		return bandList;
	}

	public void setBandList(List<BandBean> bandList) {
		this.bandList = bandList;
	}

	public StockDataBean getStartKLine() {
		return startKLine;
	}

	public void setStartKLine(StockDataBean startKLine) {
		this.startKLine = startKLine;
	}

	public StockDataBean getEndKLine() {
		return endKLine;
	}

	public void setEndKLine(StockDataBean endKLine) {
		this.endKLine = endKLine;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public PowerBean getPrev() {
		return prev;
	}

	public void setPrev(PowerBean prev) {
		this.prev = prev;
	}

	public PowerBean getNext() {
		return next;
	}

	public void setNext(PowerBean next) {
		this.next = next;
	}

	public BigDecimal getBandMaxHigh() {
		return bandMaxHigh;
	}

	public void setBandMaxHigh(BigDecimal bandMaxHigh) {
		this.bandMaxHigh = bandMaxHigh;
	}

	public BigDecimal getBandMaxLow() {
		return bandMaxLow;
	}

	public void setBandMaxLow(BigDecimal bandMaxLow) {
		this.bandMaxLow = bandMaxLow;
	}
}
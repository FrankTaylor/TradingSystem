package com.huboyi.engine.indicators.technology.pattern.bean;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.huboyi.util.JAXBHelper;

/**
 * 行情波段Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/24
 * @version 1.0
 */
@JsonPropertyOrder(value = {"bandType", "top", "bottom", "nums", "upNum", "downNum", "totalVolume", "upTotalVolume", "downTotalVolume", "totalAmount", "upTotalAmount", "downToalAmount"}, alphabetic = false)
public class BandBean implements Serializable {

	private static final long serialVersionUID = 4896217482786137418L;

	/**
	 * 用于标示波段方向的枚举类。
	 * 
	 * @author FrankTaylor <mailto:hubin@300.cn>
	 * @since 2014/10/24
	 * @version 1.0
	 */
	public static enum BandType {
		/** 向上。*/
		UP,
		/** 向下。*/
		DOWN;
	}
	
	/** 波段方向。*/
	@JsonProperty(value = "band_type", required = true)
	private BandType bandType;
	
	/** 顶分型。*/
	@JsonProperty(value = "top", required = true)
	private FractalBean top;
	
	/** 底分型。*/
	@JsonProperty(value = "bottom", required = true)
	private FractalBean bottom;
	
	// --- 波段内K线数量信息 ---
	/** 波段内K线的数量。*/
	@JsonProperty(value = "nums", required = true)
	private int nums;
	
	/** 波段内阳线的数量。*/
	@JsonProperty(value = "up_nums", required = true)
	private int upNums;
	
	/** 波段内阴线的数量。*/
	@JsonProperty(value = "down_nums", required = true)
	private int downNums;
	
	// --- 波段内K线成交量信息 ---
	/** 波段内全部K线成交量的总和。*/
	@JsonProperty(value = "total_volume", required = true)
	private BigDecimal totalVolume;
	
	/** 波段内全部阳线成交量的总和。*/
	@JsonProperty(value = "up_total_volume", required = true)
	private BigDecimal upTotalVolume;
	
	/** 波段内全部阴线成交量的总和。*/
	@JsonProperty(value = "down_total_volume", required = true)
	private BigDecimal downTotalVolume;
	
	// --- 波段内K线成交额信息 ---
	/** 波段内全部K线成交额的总和。*/
	@JsonProperty(value = "total_amount", required = true)
	private BigDecimal totalAmount;
	
	/** 波段内全部阳线成交额的总和。*/
	@JsonProperty(value = "up_total_amount", required = true)
	private BigDecimal upTotalAmount;
	
	/** 波段内全部阴线成交额的总和。*/
	@JsonProperty(value = "down_total_amount", required = true)
	private BigDecimal downTotalAmount;
	
	// --- 其他信息 ---
	/** 上一个波段。*/
	private BandBean prev;
	/** 下一个波段。*/
	private BandBean next;
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("band_type = ").append(bandType).append("\n")
		.append("\t").append("top = ").append(top.getCenter().getDate()).append("\n")
		.append("\t").append("bottom = ").append(bottom.getCenter().getDate()).append("\n")
		.append("\t").append("nums = ").append(nums).append("\n")
		.append("\t").append("up_nums = ").append(upNums).append("\n")
		.append("\t").append("down_nums = ").append(downNums).append("\n")
		.append("\t").append("total_volume = ").append(totalVolume).append("\n")
		.append("\t").append("up_total_volume = ").append(upTotalVolume).append("\n")
		.append("\t").append("down_total_volume = ").append(downTotalVolume).append("\n")
		.append("\t").append("total_amount = ").append(totalAmount).append("\n")
		.append("\t").append("up_total_amount = ").append(upTotalAmount).append("\n")
		.append("\t").append("down_total_amount = ").append(downTotalAmount).append("\n")
		.append("]\n");
		return builder.toString();
	}
	
	/**
	 * 波段间顶分型价格比较。
	 * 
	 * @param o 被比较波段
	 * @param priceType c:收盘价（默认）；h:最高价；o:开盘价；l:最低价
	 * @return int
	 */
	public int compareBandTop (BandBean o, String priceType) {
		priceType = priceType == null ? "c" : priceType;
		if (priceType.equalsIgnoreCase("c")) {
			return getTop().getCenter().getClose().compareTo(o.getTop().getCenter().getClose());
		} else if (priceType.equalsIgnoreCase("h")) {
			return getTop().getCenter().getHigh().compareTo(o.getTop().getCenter().getHigh());
		} else if (priceType.equalsIgnoreCase("o")) {
			return getTop().getCenter().getOpen().compareTo(o.getTop().getCenter().getOpen());
		} else if (priceType.equalsIgnoreCase("l")) {
			return getTop().getCenter().getLow().compareTo(o.getTop().getCenter().getLow());
		} else {
			return getTop().getCenter().getClose().compareTo(o.getTop().getCenter().getClose());
		}
	}
	
	/**
	 * 波段间底分型价格比较。
	 * 
	 * @param o 被比较波段
	 * @param priceType c:收盘价（默认）；h:最高价；o:开盘价；l:最低价
	 * @return int
	 */
	public int compareBandBottom (BandBean o, String priceType) {
		priceType = priceType == null ? "c" : priceType;
		if (priceType.equalsIgnoreCase("c")) {
			return getBottom().getCenter().getClose().compareTo(o.getBottom().getCenter().getClose());
		} else if (priceType.equalsIgnoreCase("h")) {
			return getBottom().getCenter().getHigh().compareTo(o.getBottom().getCenter().getHigh());
		} else if (priceType.equalsIgnoreCase("o")) {
			return getBottom().getCenter().getOpen().compareTo(o.getBottom().getCenter().getOpen());
		} else if (priceType.equalsIgnoreCase("l")) {
			return getBottom().getCenter().getLow().compareTo(o.getBottom().getCenter().getLow());
		} else {
			return getBottom().getCenter().getClose().compareTo(o.getBottom().getCenter().getClose());
		}
	}
	
	/**
	 * 该波段与另一根波段比较是否是同一根波段。
	 * 
	 * @param o BandBean
	 * @return boolean true：是同一根波段；false：不是同一根波段
	 */
	public boolean isOneAndTheSame (BandBean o) {
		if (getBandType() == o.getBandType()) {
			if (
					(getBottom().getCenter().getDate().equals(o.getBottom().getCenter().getDate())) && 
					(getTop().getCenter().getDate().equals(o.getTop().getCenter().getDate()))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 把JavaBean转换为默认格式的JSON。 
	 * 
	 * @return String
	 * @throws JsonProcessingException
	 */
	public String toDefaultJson () 
	throws JsonProcessingException {
		return JAXBHelper.javaToDefaultJson(this);
	}
	
	/**
	 * 把JavaBean转换为mimi格式的JSON，建议在开发中采用此方法，以提升部分性能。 
	 * 
	 * @return String
	 * @throws JsonProcessingException
	 */
	public String toMiniJson () 
	throws JsonProcessingException {
		return JAXBHelper.javaToMiniJson(this);
	}
	
	/*---------------- 静态方法 ---------------*/
	
	/**
	 * 把JSON转换为JavaBean。
	 * 
	 * @param json json信息
	 * @return MACDBean
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static BandBean 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, BandBean.class);
	}

	// --- get method and set method ---
	
	public BandType getBandType() {
		return bandType;
	}

	public void setBandType(BandType bandType) {
		this.bandType = bandType;
	}

	public FractalBean getTop() {
		return top;
	}

	public void setTop(FractalBean top) {
		this.top = top;
	}

	public FractalBean getBottom() {
		return bottom;
	}

	public void setBottom(FractalBean bottom) {
		this.bottom = bottom;
	}

	public int getNums() {
		return nums;
	}

	public void setNums(int nums) {
		this.nums = nums;
	}

	public int getUpNums() {
		return upNums;
	}

	public void setUpNums(int upNums) {
		this.upNums = upNums;
	}

	public int getDownNums() {
		return downNums;
	}

	public void setDownNums(int downNums) {
		this.downNums = downNums;
	}

	public BigDecimal getTotalVolume() {
		return totalVolume;
	}

	public void setTotalVolume(BigDecimal totalVolume) {
		this.totalVolume = totalVolume;
	}

	public BigDecimal getUpTotalVolume() {
		return upTotalVolume;
	}

	public void setUpTotalVolume(BigDecimal upTotalVolume) {
		this.upTotalVolume = upTotalVolume;
	}

	public BigDecimal getDownTotalVolume() {
		return downTotalVolume;
	}

	public void setDownTotalVolume(BigDecimal downTotalVolume) {
		this.downTotalVolume = downTotalVolume;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getUpTotalAmount() {
		return upTotalAmount;
	}

	public void setUpTotalAmount(BigDecimal upTotalAmount) {
		this.upTotalAmount = upTotalAmount;
	}

	public BigDecimal getDownTotalAmount() {
		return downTotalAmount;
	}

	public void setDownTotalAmount(BigDecimal downTotalAmount) {
		this.downTotalAmount = downTotalAmount;
	}

	public BandBean getNext() {
		return next;
	}

	public void setNext(BandBean next) {
		this.next = next;
	}

	public BandBean getPrev() {
		return prev;
	}

	public void setPrev(BandBean prev) {
		this.prev = prev;
	}
}
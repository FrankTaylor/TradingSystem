package com.huboyi.engine.merge.bean;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

import com.huboyi.util.JAXBHelper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 装载股票数据的Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/18
 * @version 1.0
 */
@JsonPropertyOrder(value = {"time", "open", "high", "low", "close", "volume", "amount"}, alphabetic = false)
public class Bar implements Serializable, Cloneable {

	private static final long serialVersionUID = -124165835666555529L;

	/*---------- 时间信息 ---------*/
	
	/** 时间。*/
	@JsonProperty(value = "time", required = true)
	private Long time;
	
	/*---------- 价格信息 ---------*/
	
	/** 开盘价。*/
	@JsonProperty(value = "open", required = true)
	private BigDecimal open;
	/** 最高价。*/
	@JsonProperty(value = "high", required = true)
	private BigDecimal high;
	/** 最低价。*/
	@JsonProperty(value = "low", required = true)
	private BigDecimal low;
	/** 收盘价。*/
	@JsonProperty(value = "close", required = true)
	private BigDecimal close;
	
	/*---------- 成交信息 ---------*/
	
	/** 成交量。*/
	@JsonProperty(value = "volume", required = true)
	private BigDecimal volume;
	/** 成交额。*/
	@JsonProperty(value = "amount", required = true)
	private BigDecimal amount;

	/*---------- 其他信息 ---------*/
	
	/** 上一个行情数据信息。*/
	private Bar prev;
	/** 下一个行情数据信息。*/
	private Bar next;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("time = ").append(time).append("\n")
		.append("\t").append("open = ").append(open).append("\n")
		.append("\t").append("high = ").append(high).append("\n")
		.append("\t").append("low = ").append(low).append("\n")
		.append("\t").append("close = ").append(close).append("\n")
		.append("\t").append("volume = ").append(volume).append("\n")
		.append("\t").append("amount = ").append(amount).append("\n")
		.append("\t").append("prev_time = ").append((null != prev) ? prev.getTime() : null).append("\n")
		.append("\t").append("next_time = ").append((null != next) ? next.getTime() : null).append("\n")
		.append("]\n");
		return builder.toString();
	}
	
	@Override  
    public Object clone() throws CloneNotSupportedException {  
        return super.clone();  
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
	 * @return StockDataBean
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static Bar 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, Bar.class);
	}
	
	// --- get method and set method ---
	
	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
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

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getVolume() {
		return volume;
	}

	public void setVolume(BigDecimal volume) {
		this.volume = volume;
	}

	public Bar getPrev() {
		return prev;
	}

	public void setPrev(Bar prev) {
		this.prev = prev;
	}

	public Bar getNext() {
		return next;
	}

	public void setNext(Bar next) {
		this.next = next;
	}
}
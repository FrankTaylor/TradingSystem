package com.huboyi.engine.load.bean;

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
 * @since 2014/10/16
 * @version 1.0
 */
@JsonPropertyOrder(value = {"date", "open", "high", "low", "close", "volume", "amount"}, alphabetic = false)
public class StockDataBean implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 5022341905217913161L;

	/*---------- 时间信息 ---------*/
	
	/** 日期。*/
	@JsonProperty(value = "date", required = true)
	private Integer date;
	
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
	private StockDataBean prev;
	/** 下一个行情数据信息。*/
	private StockDataBean next;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("date = ").append(date).append("\n")
		.append("\t").append("open = ").append(open).append("\n")
		.append("\t").append("high = ").append(high).append("\n")
		.append("\t").append("low = ").append(low).append("\n")
		.append("\t").append("close = ").append(close).append("\n")
		.append("\t").append("volume = ").append(volume).append("\n")
		.append("\t").append("amount = ").append(amount).append("\n")
		.append("\t").append("prev_date = ").append((null != prev) ? prev.getDate() : null).append("\n")
		.append("\t").append("next_date = ").append((null != next) ? next.getDate() : null).append("\n")
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
	public static StockDataBean 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, StockDataBean.class);
	}
	
	// --- get method and set method ---
	
	public Integer getDate() {
		return date;
	}

	public void setDate(Integer date) {
		this.date = date;
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

	public StockDataBean getPrev() {
		return prev;
	}

	public void setPrev(StockDataBean prev) {
		this.prev = prev;
	}

	public StockDataBean getNext() {
		return next;
	}

	public void setNext(StockDataBean next) {
		this.next = next;
	}
}
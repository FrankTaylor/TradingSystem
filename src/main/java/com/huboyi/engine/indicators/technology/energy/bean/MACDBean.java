package com.huboyi.engine.indicators.technology.energy.bean;

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
 * MACD指标。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/20
 * @version 1.0
 */
@JsonPropertyOrder(value = {"date", "diff", "dea", "macd"}, alphabetic = false)
public class MACDBean implements Serializable {

	private static final long serialVersionUID = -5002918405781594515L;

	/** 日期。*/
	@JsonProperty(value = "date", required = true)
	private Integer date = 19700101;
	
	/** 短期均线与长期均线的差值。*/
	@JsonProperty(value = "diff", required = true)
	private BigDecimal diff = BigDecimal.valueOf(0);
	
	/** 短期均线与长期均线的差值的平均值。*/
	@JsonProperty(value = "dea", required = true)
	private BigDecimal dea = BigDecimal.valueOf(0);
	
	/** diff与dea差值。*/
	@JsonProperty(value = "macd", required = true)
	private BigDecimal macd = BigDecimal.valueOf(0);
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("date = ").append(date).append("\n")
		.append("\t").append("diff = ").append(diff).append("\n")
		.append("\t").append("dea = ").append(dea).append("\n")
		.append("\t").append("macd = ").append(macd).append("\n")
		.append("]\n");
		return builder.toString();
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
	public static MACDBean 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, MACDBean.class);
	}

	// --- get method and set method ---
	
	public Integer getDate() {
		return date;
	}

	public void setDate(Integer date) {
		this.date = date;
	}

	public BigDecimal getDiff() {
		return diff;
	}

	public void setDiff(BigDecimal diff) {
		this.diff = diff;
	}

	public BigDecimal getDea() {
		return dea;
	}

	public void setDea(BigDecimal dea) {
		this.dea = dea;
	}

	public BigDecimal getMacd() {
		return macd;
	}

	public void setMacd(BigDecimal macd) {
		this.macd = macd;
	}
}
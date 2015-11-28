package com.huboyi.indicators.technology.bean.trend;

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
 * 布林带指标。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
@JsonPropertyOrder(value = {"date", "mdValue", "upValue", "middleValue", "downValue"}, alphabetic = false)
public class BollBean implements Serializable {

	private static final long serialVersionUID = 5717556981398863110L;

	/** 日期。*/
	@JsonProperty(value = "date", required = true)
	private Long date = 19700101000000000L;
	
	/** 波动率。*/
	@JsonProperty(value = "md_value", required = true)
	private BigDecimal mdValue = BigDecimal.valueOf(0);
	
	/** 计算后的上轨值。*/
	@JsonProperty(value = "up_value", required = true)
	private BigDecimal upValue = BigDecimal.valueOf(0);
	
	/** 计算后的中轨值。*/
	@JsonProperty(value = "middle_value", required = true)
	private BigDecimal middleValue = BigDecimal.valueOf(0);
	
	/** 计算后的下轨值。*/
	@JsonProperty(value = "down_value", required = true)
	private BigDecimal downValue = BigDecimal.valueOf(0);
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("date = ").append(date).append("\n")
		.append("\t").append("md_value = ").append(mdValue).append("\n")
		.append("\t").append("up_value = ").append(upValue).append("\n")
		.append("\t").append("middle_value = ").append(middleValue).append("\n")
		.append("\t").append("down_value = ").append(downValue).append("\n")
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
	 * @return BollBean
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static BollBean 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, BollBean.class);
	}
	
	// --- get method and set method ---
	
	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public BigDecimal getMdValue() {
		return mdValue;
	}

	public void setMdValue(BigDecimal mdValue) {
		this.mdValue = mdValue;
	}

	public BigDecimal getUpValue() {
		return upValue;
	}

	public void setUpValue(BigDecimal upValue) {
		this.upValue = upValue;
	}

	public BigDecimal getMiddleValue() {
		return middleValue;
	}

	public void setMiddleValue(BigDecimal middleValue) {
		this.middleValue = middleValue;
	}

	public BigDecimal getDownValue() {
		return downValue;
	}

	public void setDownValue(BigDecimal downValue) {
		this.downValue = downValue;
	}
}
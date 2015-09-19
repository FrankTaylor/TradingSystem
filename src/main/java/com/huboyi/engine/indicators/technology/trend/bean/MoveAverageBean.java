package com.huboyi.engine.indicators.technology.trend.bean;

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
 * 普通平均线指标。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/20
 * @version 1.0
 */
@JsonPropertyOrder(value = {"date", "source", "avg"}, alphabetic = false)
public class MoveAverageBean implements Serializable {

	private static final long serialVersionUID = 3897316158075774750L;

	/** 日期。*/
	@JsonProperty(value = "date", required = true)
	private Long date = 19700101000000000L;
	
	/** 计算前的值。*/
	@JsonProperty(value = "source", required = true)
	private BigDecimal source = BigDecimal.valueOf(0);
	
	/** 计算后的值。*/
	@JsonProperty(value = "avg", required = true)
	private BigDecimal avg = BigDecimal.valueOf(0);
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("date = ").append(date).append("\n")
		.append("\t").append("source = ").append(source).append("\n")
		.append("\t").append("avg = ").append(avg).append("\n")
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
	 * @return MoveAverageBean
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static MoveAverageBean 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, MoveAverageBean.class);
	}
	
	// --- get method and set method ---
	
	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public BigDecimal getSource() {
		return source;
	}

	public void setSource(BigDecimal source) {
		this.source = source;
	}

	public BigDecimal getAvg() {
		return avg;
	}

	public void setAvg(BigDecimal avg) {
		this.avg = avg;
	}
}
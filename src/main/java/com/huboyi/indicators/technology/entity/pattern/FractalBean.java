package com.huboyi.indicators.technology.entity.pattern;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.huboyi.data.entity.StockDataBean;
import com.huboyi.indicators.technology.constant.FractalType;
import com.huboyi.util.JAXBHelper;

/**
 * 顶/底分型。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
@JsonPropertyOrder(value = {"fractalType", "left", "center", "right"}, alphabetic = false)
public class FractalBean implements Serializable {

	private static final long serialVersionUID = 1391851193561639169L;
	
	/** 分型类别。*/
	@JsonProperty(value = "fractal_type", required = true)
	private FractalType fractalType;
	
	/** 分型左边的行情数据。*/
	@JsonProperty(value = "left", required = true)
	private StockDataBean left;
	
	/** 分型中间的行情数据。*/
	@JsonProperty(value = "center", required = true)
	private StockDataBean center;
	
	/** 分型右边的行情数据。*/
	@JsonProperty(value = "right", required = true)
	private StockDataBean right;
	
	/** 紧接着的上一个分型。*/
	private FractalBean prev;
	/** 紧接着的下一个分型。*/
	private FractalBean next;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("fractal_type = ").append(fractalType).append("\n")
		.append("\t").append("left = ").append(left).append("\n")
		.append("\t").append("center = ").append(center).append("\n")
		.append("\t").append("right = ").append(right).append("\n")
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
	public static FractalBean 
	jsonToJava (String json) throws JsonParseException, JsonMappingException, IOException {
		return JAXBHelper.jsonToJava(json, FractalBean.class);
	}

	// --- get method and set method ---
	
	public FractalType getFractalType() {
		return fractalType;
	}

	public void setFractalType(FractalType fractalType) {
		this.fractalType = fractalType;
	}

	public StockDataBean getLeft() {
		return left;
	}

	public void setLeft(StockDataBean left) {
		this.left = left;
	}

	public StockDataBean getCenter() {
		return center;
	}

	public void setCenter(StockDataBean center) {
		this.center = center;
	}

	public StockDataBean getRight() {
		return right;
	}

	public void setRight(StockDataBean right) {
		this.right = right;
	}

	public FractalBean getPrev() {
		return prev;
	}

	public void setPrev(FractalBean prev) {
		this.prev = prev;
	}

	public FractalBean getNext() {
		return next;
	}

	public void setNext(FractalBean next) {
		this.next = next;
	}
}